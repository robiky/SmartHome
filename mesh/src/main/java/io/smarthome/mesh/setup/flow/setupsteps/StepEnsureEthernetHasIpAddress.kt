package io.smarthome.mesh.setup.flow.setupsteps

import io.smarthome.firmwareprotos.ctrl.Network
import io.smarthome.firmwareprotos.ctrl.Network.InterfaceType
import io.smarthome.mesh.R
import io.smarthome.mesh.common.android.livedata.nonNull
import io.smarthome.mesh.common.android.livedata.runBlockOnUiThreadAndAwaitUpdate
import io.smarthome.mesh.common.truthy
import io.smarthome.mesh.setup.flow.*
import io.smarthome.mesh.setup.flow.context.SetupContexts
import io.smarthome.mesh.setup.flow.modules.FlowUiDelegate
import io.smarthome.mesh.setup.ui.DialogSpec.ResDialogSpec
import kotlinx.coroutines.delay
import mu.KotlinLogging


class StepEnsureEthernetHasIpAddress(private val flowUi: FlowUiDelegate) : MeshSetupStep() {

    private val log = KotlinLogging.logger {}

    override suspend fun doRunStep(ctxs: SetupContexts, scopes: Scopes) {
        val targetXceiver = ctxs.requireTargetXceiver()

        suspend fun findEthernetInterface(): Network.InterfaceEntry? {
            val ifaceListReply = targetXceiver.sendGetInterfaceList().throwOnErrorOrAbsent()
            return ifaceListReply.interfacesList.firstOrNull { it.type == InterfaceType.ETHERNET }
        }

        val ethernet = findEthernetInterface()
        requireNotNull(ethernet)

        val reply = targetXceiver.sendGetInterface(ethernet.index).throwOnErrorOrAbsent()
        val iface = reply.`interface`
        for (addyList in listOf(iface.ipv4Config.addressesList, iface.ipv6Config.addressesList)) {

            val address = addyList.firstOrNull {
                it.address.v4.address.truthy() || it.address.v6.address.truthy()
            }
            if (address != null) {
                log.debug { "IP address on ethernet (interface ${ethernet.index}) found: $address" }
                return
            }
        }

        val result = flowUi.dialogTool.dialogResultLD.nonNull(scopes).runBlockOnUiThreadAndAwaitUpdate(scopes) {
            flowUi.dialogTool.newDialogRequest(
                ResDialogSpec(
                    R.string.p_connecttocloud_xenon_gateway_needs_ethernet,
                    android.R.string.ok
                )
            )
        }

        log.info { "result from awaiting on 'ethernet must be plugged in dialog: $result" }

        flowUi.dialogTool.clearDialogResult()
        delay(500)
        throw ExpectedFlowException("Ethernet connection not plugged in; user prompted.")
    }

}
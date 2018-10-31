package com.alibaba.idst.nls.dialog.unit.func;

import com.alibaba.fastjson.JSON;
import com.alibaba.idst.nls.dialog.unit.service.UnitConversationService;
import com.alibaba.idst.nls.uds.annotation.DialogFunc;
import com.alibaba.idst.nls.uds.annotation.Inject;
import com.alibaba.idst.nls.uds.context.DialogSession;
import com.alibaba.idst.nls.uds.log.DialogLogger;
import com.alibaba.idst.nls.uds.response.DialogResultElement;
import com.alibaba.idst.nlu.response.common.NluResultElement;
import com.alibaba.idst.nlu.response.common.BaseSlot;
import com.alibaba.idst.nlu.response.slot.BasicSlot;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@DialogFunc(domain = "unit_conversation", intent = "query_length_conversation")
public class LengthConversationFn implements Function<NluResultElement, DialogResultElement> {
    @Inject
    private DialogLogger logger;

    @Inject
    private DialogSession session;

    @Override
    public DialogResultElement apply(NluResultElement nluResultElement) {
        Map<String, List<BaseSlot>> contextSlots = session.getSlots();
        Map<String, List<BaseSlot>> currentSlots = nluResultElement.getSlotMap();
        if (contextSlots != null) {
            contextSlots.putAll(currentSlots);
        } else {
            contextSlots = currentSlots;
        }

        String reply = null;
        if (!contextSlots.containsKey("SourceLengthUnit")){
            reply = "请问您要转换哪个单位？";
            return DialogResultElement.builder().displayText(reply).build();
        }

        if (!contextSlots.containsKey("TargetLengthUnit")){
            reply = "请问您要转换成哪个单位？";
            return DialogResultElement.builder().displayText(reply).build();
        }

        String sourceLengthUnit = ((BasicSlot)contextSlots.get("SourceLengthUnit").get(0)).getNorm();
        String targetLengthUnit = ((BasicSlot)contextSlots.get("TargetLengthUnit").get(0)).getNorm();

        if (!contextSlots.containsKey("Number")){
            reply = String.format("请问您想要把多少%s转成成%s", sourceLengthUnit, targetLengthUnit);
            return DialogResultElement.builder().displayText(reply).build();
        }

        int inputNum = Integer.valueOf(((BasicSlot)contextSlots.get("Number").get(0)).getNorm());
        Double convertNum = UnitConversationService.convertUnitValue(sourceLengthUnit, targetLengthUnit, inputNum);
        logger.info("context slots: " + JSON.toJSONString(contextSlots));

        if (Objects.isNull(convertNum)){
            reply = "您好，您输入的单位我们还不支持，你可以询问\"现在支持哪些换算单位\"来询问当前我们支持的换算单位";
            return DialogResultElement.builder().displayText(reply).build();
        }

        String resultNum = UnitConversationService.changeOutputNumber(convertNum);
        reply = String.format("%s%s等于%s%s", inputNum, sourceLengthUnit, resultNum, targetLengthUnit);
        return DialogResultElement.builder().displayText(reply).build();
    }
}

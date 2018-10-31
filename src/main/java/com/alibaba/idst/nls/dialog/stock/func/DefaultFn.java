package com.alibaba.idst.nls.dialog.stock.func;

import com.alibaba.idst.nls.uds.annotation.DialogFunc;
import com.alibaba.idst.nls.uds.response.DialogResultElement;
import com.alibaba.idst.nlu.response.common.NluResultElement;

import java.util.function.Function;

@DialogFunc(domain = "unknown", intent = "unknown")
public class DefaultFn implements Function<NluResultElement, DialogResultElement> {
    @Override
    public DialogResultElement apply(NluResultElement nluResultElement) {
        final String reply = "您好,我不太明白您的意思,我们现在支持股票查询，制式转换，如您可以问阿里巴巴股价多少，10公里等于多少米";
        return DialogResultElement.builder().displayText(reply).build();
    }
}

package com.alibaba.idst.nls.dialog.stock.func;

import com.alibaba.fastjson.JSON;
import com.alibaba.idst.nls.dialog.stock.service.StockService;
import com.alibaba.idst.nls.uds.annotation.DialogFunc;
import com.alibaba.idst.nls.uds.annotation.Inject;
import com.alibaba.idst.nls.uds.context.DialogSession;
import com.alibaba.idst.nls.uds.log.DialogLogger;
import com.alibaba.idst.nls.uds.response.DialogResultElement;
import com.alibaba.idst.nlu.response.common.NluResultElement;
import com.alibaba.idst.nlu.response.common.BaseSlot;
import com.alibaba.idst.nlu.response.slot.BasicSlot;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@DialogFunc(domain = "stock", intent = "query_stock_price")
public class StockFn implements Function<NluResultElement, DialogResultElement> {
    @Inject
    private DialogLogger logger;

    @Inject
    private DialogSession session;

    /**
     * 公司名称词槽的key
     */
    final static String companySlot = "PUBLIC_COMPANY";

    /**
     * 沪深两市的股市code
     */
    final static String hsMarketCode = "hs";

    /**
     * 美股股市code
     */
    final static String usMarketCode = "us";

    @Override
    public DialogResultElement apply(NluResultElement nluResultElement) {
        Long startTime = System.currentTimeMillis();

        Map<String, List<BaseSlot>> contextSlots = session.getSlots();
        Map<String, List<BaseSlot>> currentSlots = nluResultElement.getSlotMap();
        if(contextSlots != null) {
            contextSlots.putAll(currentSlots);
        } else {
            contextSlots = currentSlots;
        }

        logger.info("context slots: " + JSON.toJSONString(contextSlots));
        String reply = null;
        if(contextSlots.containsKey(companySlot)) {
            String publicCompany = ((BasicSlot)contextSlots.get(companySlot).get(0)).getNorm();
            String userInput = ((BasicSlot)contextSlots.get(companySlot).get(0)).getRaw();

            StockService.Stock stock = StockService.getStockPrice(publicCompany);
            if (!stock.isExist()){
                reply = String.format("抱歉，未找到%s这个公司的股票信息，请确认该上市公司是否存在。", userInput);
                return DialogResultElement.builder().displayText(reply).build();
            }

            DecimalFormat df = new DecimalFormat("0.00");
            if (Objects.equals(hsMarketCode, stock.getMarket())){
                reply = String.format("%s每股价值%s人民币", publicCompany, df.format(stock.getMarketPrice()));
            }else if (Objects.equals(usMarketCode, stock.getMarket())){
                reply = String.format("%s每股价值%s美元，折合人民币%s一股", publicCompany, df.format(stock.getMarketPrice()), df.format(stock.getCnPrice()));
            }else{
                reply = String.format("%s每股价值%s港元，折合人民币%s一股", publicCompany, df.format(stock.getMarketPrice()), df.format(stock.getCnPrice()));
            }
        } else {
            reply = "你想查找哪个上市公司的?";
        }

        logger.info(String.format("StockFn : cost time %d, nluResultElement is %s", System.currentTimeMillis() - startTime, JSON.toJSONString(nluResultElement)));
        return DialogResultElement.builder().displayText(reply).build();
    }
}

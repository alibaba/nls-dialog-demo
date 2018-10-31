package com.alibaba.idst.nls.dialog.unit.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.idst.nls.dialog.stock.service.StockService;

import java.util.Objects;

public class UnitConversationService {
    public static Double convertUnitValue(String sourceUnitName, String targetUnitName, long num){
        JSONObject unitToValueMap = getUnitValueMap();
        if (Objects.isNull(unitToValueMap)){
            return null;
        }

        Double sourceValue = getUnitValueByName(sourceUnitName, unitToValueMap);
        Double targetValue = getUnitValueByName(targetUnitName, unitToValueMap);
        if (Objects.isNull(sourceValue) || Objects.isNull(targetValue)){
            return null;
        }

        double value = sourceValue / targetValue * num;
        return value;
    }

    public static JSONObject getUnitValueMap(){
        final String key = "unit_to_value_map";
        String value = StockService.getValueFromRedis(key);
        if (Objects.isNull(value) || value.length() == 0){
            return null;
        }

        JSONObject unitToValueMap = JSON.parseObject(value);
        return unitToValueMap;
    }

    public static Double getUnitValueByName(String unitName, JSONObject unitToValueMap){
        if (!unitToValueMap.containsKey(unitName)){
            return null;
        }

        return unitToValueMap.getDouble(unitName);
    }

    public static String changeOutputNumber(Double num){
        if (Objects.isNull(num)){
            return null;
        }

        num = ((long)(num.doubleValue() * 10000)) / 10000.00;
        String numstr = num.toString();
        int lastIndex = numstr.length() - 1;
        for (int i = numstr.length() - 1; i >= 0; i--){
            if (Objects.equals(numstr.charAt(i), '.')){
                lastIndex = i - 1;
                break;
            }

            if (!Objects.equals(numstr.charAt(i), '0')){
                lastIndex = i;
                break;
            }
        }

        return numstr.substring(0, lastIndex + 1);
    }
}

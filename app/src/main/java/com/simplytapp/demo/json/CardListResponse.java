package com.simplytapp.demo.json;


/*
 *  JSON in the getCardList response looks like this:
 *
 *   {
 *      "status":"0",
 *      "data":{
 *         "cards":[
 *            {
 *               "EXP":"Sat Jan 02 16:28:24 UTC 2016",
 *               "ID":"11111111",
 *               "NAME":"",
 *               "DISABLED":"N",
 *               "CARD_SPEC":"22222222",
 *               "PAN":"XXXXXXXXXXXX1234",
 *               "ACCESS_URL":"",
 *               "BRAND_ID":"33333333",
 *               "BRAND":"Mobile PayPass",
 *               "LOGO":"https://www.simplytapp.com/accounts/appletImages/33333333.png",
 *               "HASH": MD5 HASH,
 *               "VERSION": SPEC_VER,
 *               "SECRET":"",
 *               "TOKEN":"",
 *               "DESCRIPTION":""
 *            }
 *         ]
 *      }
 *   }
 */
public class CardListResponse {

    private int status;
    private ReponseData data;

    public ReponseData getData() {
        return data;
    }

    public int getStatus() {
        return status;
    }
}
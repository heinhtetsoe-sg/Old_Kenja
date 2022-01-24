<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl400qModel extends Model {
    var $field      = array();
    var $data       = array();
    var $cmd;
    var $file;
    var $control;           //コントロールマスタ
    var $staffcd;           //登録者コード
    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    /**
     * メンバ変数の初期化
     */
    function knjl400qModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                            //登録者コード
    }
    function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if(VARS::get("SEND_PRGID")){
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;


        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll(); //プロパティファイル
        
        $this->field = array("CHECK"    =>  VARS::post("CHECK"),
                             
                             );

    }
    function &getMainModel()
    {
    }

    //CSV出力
    function getExecModel()
    {
        $header_data = "";
        
        $db = Query::dbCheckOut();
        
        //データカウント
        $query = knjl400qQuery::getExportData($this->field["CHECK"], "1");
        $cnt = $db->getOne($query);
        
        if($cnt > 0){
            $contents  = "受験番号,氏名,カナ,フリガナ1,フリガナ2,性別,中学コード,中学校名,生年月日,卒業,";
            //成績・志望高除く
            if(empty($this->field["CHECK"]) || !in_array("5", $this->field["CHECK"])){
                $contents .= "英1,数1,国1,欠(試験),英欠,";
                $contents .= "数欠,国欠,合計,平均点,判定,偏差値,";
                $contents .= "地区英順位,地区数順位,地区国順位,地区合計順位,全体英語順位,全体数学順位,全体国語順位,全体合計順位,";
                $contents .= "第1志望高CD,第1志望高,第2志望高CD,第2志望高,第3志望高CD,第3志望高,";
            }
            $contents .= "進8受験番号,進83科偏差値,進9受験番号,進93科偏差値,試験会場,会場地区名,会場略名,";
            $contents .= "国内海外区分,郵送窓口区分,個人団体区分,校内生番号,欠席(願書),団体コード,団体名,";
            $contents .= "郵便番号,住所1,住所2,電話番号,電話番号2\n";
            
            $query = knjl400qQuery::getExportData($this->field["CHECK"]);
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                
                $contents .= $row["SAT_NO"].",".$row["NAME1"].",".$row["NAME_KANA"].",".$row["KANA1"].",".$row["KANA2"].",".$row["SEX"].",".$row["SCHOOLCD"].",".$row["FINSCHOOL_NAME_ABBV"].",".$row["BIRTHDAY"].",".$row["GRADE"].",";
                if(empty($this->field["CHECK"]) || !in_array("5", $this->field["CHECK"])){
                    $contents .= $row["SCORE_ENGLISH"].",".$row["SCORE_MATH"].",".$row["SCORE_JAPANESE"].",".$row["ABSENCE"].",".$row["ABSENCE_ENGLISH"].",";
                    $contents .= $row["ABSENCE_MATH"].",".$row["ABSENCE_JAPANESE"].",".$row["SCORE_TOTAL"].",".$row["AVERAGE"].",".$row["JUDGE"].",".$row["DEVIATION"].",";
                    $contents .= $row["AREA_RANK_ENGLISH"].",".$row["AREA_RANK_MATH"].",".$row["AREA_RANK_JAPANESE"].",".$row["AREA_RANK_TOTAL"].",".$row["ALL_RANK_ENGLISH"].",".$row["ALL_RANK_MATH"].",".$row["ALL_RANK_JAPANESE"].",".$row["ALL_RANK_TOTAL"].",";
                    $contents .= $row["HOPECD1"].",".$row["HOPE1"].",".$row["HOPECD2"].",".$row["HOPE2"].",".$row["HOPECD3"].",".$row["HOPE3"].",";
                }
                $contents .= $row["MOCK_AUG_NO"].",".$row["MOCK_AUG_DEV"].",".$row["MOCK_SEP_NO"].",".$row["MOCK_SEP_DEV"].",".$row["PLACECD"].",".$row["PLACEAREA"].",".$row["PLACENAME_SHORT"].",";
                $contents .= $row["INOUT"].",".$row["SEND"].",".$row["IND"].",".$row["INSIDERNO"].",".$row["ABSENCE2"].",".$row["GROUPCD"].",".$row["GROUPNAME"].",";
                $contents .= $row["ZIPCODE"].",".$row["ADDR1"].",".$row["ADDR2"].",".$row["TELNO1"].",".$row["TELNO2"]."\n";
                
                $header_data = true;
            }
            
            
        }
        
        Query::dbCheckIn($db);
        
        //ＣＳＶ出力
        if ($header_data) {
            $filename = sprintf("%s.csv","受験者データ");
            common::downloadContents($contents, $filename);
            
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }
    


}
//var_dump($_REQUEST);
?>

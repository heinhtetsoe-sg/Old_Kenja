<?php

require_once('for_php7.php');

/**
 * 定数定義
*/
class knjl387qModel extends Model {
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
    function knjl387qModel()
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


    }
    function &getMainModel()
    {
    }


    //実行
    function getExecModel()
    {
        $db = Query::dbCheckOut();
        
        //SAT_EXAM_NO_DATの対象受験番号が作成するデータの件数に足りるかチェック
        //SAT_EXAM_NO_DATの番号数
        $query = knjl387qQuery::getExamNoCnt();
        $examCnt = $db->getOne($query);
        
        //作成対象になる生徒件数取得
        $query = knjl387qQuery::getSchregCnt();
        $schregCnt = $db->getOne($query);
        
        if($examCnt < $schregCnt){
            $this->setMessage("設定された受験番号帯内の番号が足りません。\\n受験番号帯の設定を確認してください。");
            
            Query::dbCheckIn($db);
            
            return false;
        }
        
        //先にすでに作成されている駿中生のデータを削除
        $delquery = knjl387qQuery::deleteExam();
        $db->query($delquery);
        $query = knjl387qQuery::delSatApp();
        $db->query($query);
        
        //データ作成
        //SAT_EXAM_NO_DATの番号を取得
        $query = knjl387qQuery::getExamNoCnt("1");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $start = $row["JUKEN_NO_FROM"];
        $i = 0;
        
        //SCHREG_REGD_DAT等から学籍基礎データ取得
        $query = knjl387qQuery::getSchregData();
        $result = $db->query($query);
        
        $this->insert = array();
        $this->insert["REGISTERCD"] = STAFFCD;
        $this->insert["UPDATED"] = "sysdate()";
        
        $this->insert["YEAR"] = CTRL_YEAR;
        $this->insert["KUBUN"] = "S";
        $this->insert["SCHOOLCD"] = "2008005";
        $this->insert["GRADUATION"] = "09";
        $this->insert["PREFCD"] = "19";
        $this->insert["GROUPCD"] = "08005";
        $this->insert["PLACECD"] = "80";
        $this->insert["INPUT_DATE"] = CTRL_DATE;
        $this->insert["INOUT_KUBUN"] = "1";
        $this->insert["SEND_KUBUN"] = "2";
        $this->insert["IND_KUBUN"] = "2";
        $this->insert["TOKU_FLG"] = "0";
        $this->insert["ABSENCE"] = "1";
        
        $this->examInsert = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $this->insert["SAT_NO"] = $start + $i;
            
            $this->insert["NAME1"] = $row["NAME"];
            $this->insert["NAME2"] = str_replace("　", "", $row["NAME"]);
            
            /*$spCnt = substr_count($row["NAME"], "　");
            $sp = "　";
            for($j=1;$j<$spCnt;$j++){
                $sp .= "　";
            }
            $name = explode($sp, $row["NAME"]);*/
            
            $name = str_replace("　　", "　", $row["NAME"]);
            $name = explode("　", $name);

            $this->insert["LASTNAME"] = $name[0];
            if($name[1] != ""){
                $this->insert["FIRSTNAME"] = $name[1];
            }else{
                $this->insert["FIRSTNAME"] = "";
            }
            
            $kana = explode("　", $row["NAME_KANA"]);
            $this->insert["KANA1"] = $kana[0];
            if($kana[1] != ""){
                $this->insert["KANA2"] = $kana[1];
            }else{
                $this->insert["KANA2"] = "";
            }
            
            $this->insert["SEX"] = $row["SEX"];
            $this->insert["BIRTHDAY"] = $row["BIRTHDAY"];
            
            $this->insert["ZIPCODE"] = str_replace("-", "", $row["ZIPCD"]);
            $this->insert["ADDR1"] = $row["ADDR1"];
            $this->insert["ADDR2"] = $row["ADDR2"];
            
            $this->insert["TELNO1"] = $row["TELNO"];
            $this->insert["TELNO2"] = str_replace("-", "", $row["TELNO"]);
            
            $this->insert["INSIDERNO"] = $row["SCHREGNO"];
            
            common::_update("SAT_APP_FORM_MST", "", "", $this->insert, $db, "2");
            
            //SAT_EXAM_DATも作成したい
            $this->examInsert["REGISTERCD"] = STAFFCD;
            $this->examInsert["UPDATED"] = "sysdate()";
            $this->examInsert["YEAR"] = CTRL_YEAR;
            $this->examInsert["SAT_NO"] = $this->insert["SAT_NO"];
            $this->examInsert["ABSENCE"] = "1";
            $this->examInsert["ABSENCE_ENGLISH"] = "1";
            $this->examInsert["ABSENCE_MATH"] = "1";
            $this->examInsert["ABSENCE_JAPANESE"] = "1";
            if($this->insert["PLACECD"] == "80" && $this->insert["SCHOOLCD"] == "2008005"){ //駿中生
                $this->examInsert["AREA"] = "1";
            }else if($this->insert["INOUT_KUBUN"] == "1"){  //県内
                $this->examInsert["AREA"] = "2";
            }else{          //県外
                $this->examInsert["AREA"] = "3";
            }
            
            common::_update("SAT_EXAM_DAT", "", "", $this->examInsert, $db, "2");
            
            $i++;
            
        }
        
        Query::dbCheckIn($db);
        
        $this->setMessage("作成しました。");
        
    }
    


}
//var_dump($_REQUEST);
?>

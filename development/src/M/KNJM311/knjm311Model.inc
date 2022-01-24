<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjm311Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;       //コントロールマスタ
    var $staffcd;       //職員コード
    var $checsemf;      //学期
    var $checsemt;      //学期
    var $Datef;         //日付FROM
    var $Datet;         //日付TO

    /**
     * メンバ変数の初期化
     */
    function knjm311Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    function init()
    {
        $this->field = array("YEAR"         =>  VARS::post("YEAR"),     //日付
                             "DBNAME"       =>  VARS::post("DBNAME")    //DB変更に備えて
                     );
        $this->cmd         = VARS::request("cmd");

        if ($this->field["YEAR"] == "") $this->field["YEAR"] = CTRL_YEAR;
        if ($this->field["YEAR"] != CTRL_YEAR && $this->cmd == 'nenhenkou'){
            
            $db = Query::dbCheckOut();
            $query = knjm311Query::getCheckseme($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            
            $this->Datef  = $row["SDATE"];
            $this->Datet  = $row["EDATE"];
        } elseif ($this->field["YEAR"] == CTRL_YEAR && $this->cmd == 'nenhenkou'){
            $this->Datef  = str_replace("/","-",CTRL_DATE);
            $this->Datet  = str_replace("/","-",CTRL_DATE);
        }
        $this->Datef  = str_replace("/","-",VARS::post("DATEF"));
        $this->Datet  = str_replace("/","-",VARS::post("DATET"));
        $this->checsemf = 0;
        $this->checsemt = 0;
        if ($this->Datef == "") $this->Datef  = str_replace("/","-",CTRL_DATE);
        if ($this->Datet == "") $this->Datet  = str_replace("/","-",CTRL_DATE);

        $db = Query::dbCheckOut();
        $query = knjm311Query::getCheckseme($this);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        if ($this->control["学期数"] == 3){
            
            $this->sem3e = explode("/",$row["EDATE"]);
            if ($row["SDATE"] > $this->Datef || $row["EDATE"] < $this->Datef) $this->checsemf = 1;
            if ($row["SDATE"] > $this->Datet || $row["EDATE"] < $this->Datet) $this->checsemt = 1;
        }else {
            if ($row["SDATE"] > $this->Datef || $row["EDATE"] < $this->Datef) $this->checsemf = 1;
            if ($row["SDATE"] > $this->Datet || $row["EDATE"] < $this->Datet) $this->checsemt = 1;
        }

        //年度内チェック：エラー時は、年度と学期を今年度・今学期にする。
        if ($this->checsemf == 1){
            $this->Datef = str_replace("-","/",CTRL_DATE);
            $this->setWarning("MSG915","(開始日付が処理年度範囲外です)");
            return false;
        }
        //年度内チェック：エラー時は、年度と学期を今年度・今学期にする。
        if ($this->checsemt == 1){
            $this->Datet = str_replace("-","/",CTRL_DATE);
            $this->setWarning("MSG915","(終了日付が処理年度範囲外です)");
            return false;
        }
        $this->getPropertiesAll(); //プロパティファイル

    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>

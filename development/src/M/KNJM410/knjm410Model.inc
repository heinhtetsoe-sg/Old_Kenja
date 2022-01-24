<?php

require_once('for_php7.php');


class knjm410Model extends Model {

    var $field  = array();      //画面データ格納
    var $setdata = array();     //表示データ配列
    var $delback = array();     //削除データ配列
    var $checkdate = array();   //日付チェックデータ配列
    var $ctrldatech = array();  //日付チェックデータ配列
    var $sem1s = array();       //日付チェックデータ配列
    var $sem2s = array();       //日付チェックデータ配列
    var $sem3s = array();       //日付チェックデータ配列
    var $sem1e = array();       //日付チェックデータ配列
    var $sem2e = array();       //日付チェックデータ配列
    var $sem3e = array();       //日付チェックデータ配列
    var $schnodel;              //削除データ
    var $schcntall;             //登録データ数
    var $cmd;                   //コマンド
    var $schregno;              //サブフォーム用
    var $remark;                //サブフォーム用
    var $Year;                  //年度
    var $semester;              //学期
    var $semcheck;              //データ更新時に使用
    var $Date;                  //日付
    var $User;                  //0:管理者 1:職員
    var $control;               //コントロールマスタ

    function knjm410Model()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->cmd         = VARS::request("cmd");
        $this->Date  = VARS::post("DATE");
        if ($this->Date == "") $this->Date  = str_replace("-","/",CTRL_DATE);
        $this->checkdate = explode("/",$this->Date);

        $this->semester = 0;                //データ更新時に使用
        if ($this->control["学期数"] == 3){
            $this->sem1s = explode("/",$this->control["学期開始日付"]["1"]);
            $this->sem2s = explode("/",$this->control["学期開始日付"]["2"]);
            $this->sem3s = explode("/",$this->control["学期開始日付"]["3"]);
            $this->sem1e = explode("/",$this->control["学期終了日付"]["1"]);
            $this->sem2e = explode("/",$this->control["学期終了日付"]["2"]);
            $this->sem3e = explode("/",$this->control["学期終了日付"]["3"]);
            if ($this->sem1s[0].$this->sem1s[1].$this->sem1s[2] <= $this->checkdate[0].$this->checkdate[1].$this->checkdate[2] && $this->sem1e[0].$this->sem1e[1].$this->sem1e[2] >= $this->checkdate[0].$this->checkdate[1].$this->checkdate[2]) $this->semester = 1;
            if ($this->sem2s[0].$this->sem2s[1].$this->sem2s[2] <= $this->checkdate[0].$this->checkdate[1].$this->checkdate[2] && $this->sem2e[0].$this->sem2e[1].$this->sem2e[2] >= $this->checkdate[0].$this->checkdate[1].$this->checkdate[2]) $this->semester = 2;
            if ($this->sem3s[0].$this->sem3s[1].$this->sem3s[2] <= $this->checkdate[0].$this->checkdate[1].$this->checkdate[2] && $this->sem3e[0].$this->sem3e[1].$this->sem3e[2] >= $this->checkdate[0].$this->checkdate[1].$this->checkdate[2]) $this->semester = 3;
        }else {
            $this->sem1s = explode("/",$this->control["学期開始日付"]["1"]);
            $this->sem2s = explode("/",$this->control["学期開始日付"]["2"]);
            $this->sem1e = explode("/",$this->control["学期終了日付"]["1"]);
            $this->sem2e = explode("/",$this->control["学期終了日付"]["2"]);
            if ($this->sem1s[0].$this->sem1s[1].$this->sem1s[2] <= $this->checkdate[0].$this->checkdate[1].$this->checkdate[2] && $this->sem1e[0].$this->sem1e[1].$this->sem1e[2] >= $this->checkdate[0].$this->checkdate[1].$this->checkdate[2]) $this->semester = 1;
            if ($this->sem2s[0].$this->sem2s[1].$this->sem2s[2] <= $this->checkdate[0].$this->checkdate[1].$this->checkdate[2] && $this->sem2e[0].$this->sem2e[1].$this->sem2e[2] >= $this->checkdate[0].$this->checkdate[1].$this->checkdate[2]) $this->semester = 2;
        }
        //NO001
        $this->Year = CTRL_YEAR;

        //コントロール日付内チェック：エラー時は、年度と学期を今年度・今学期にする。
        $this->ctrldatech = explode("-",CTRL_DATE);
        if ($this->checkdate[0].$this->checkdate[1].$this->checkdate[2] > $this->ctrldatech[0].$this->ctrldatech[1].$this->ctrldatech[2]){
            $this->Date  = str_replace("-","/",CTRL_DATE);
            $this->semester = CTRL_SEMESTER;
            $this->setWarning("MSG915","(処理年度がオーバーしています)");
            return false;
        }
        $this->field = array("CHAIR"            =>  VARS::post("CHAIR"),        //講座
                             "PERIOD"           =>  VARS::post("PERIOD"),       //校時
                             "STAFF"            =>  VARS::post("STAFF"),        //担当者
                             "SCHOOLING_SEQ"    =>  VARS::post("SCHOOLING_SEQ"),//回数
                             "SCHREGNO"         =>  VARS::post("SCHREGNO"),     //学籍番号
                             "NAME_KANA"        =>  VARS::post("NAME_KANA")     //氏名（かな）
                     );
        if ($this->cmd != "update" && $this->cmd != "add" && $this->cmd != "alldel" && $this->cmd != "chdel"){
            $this->setdata = array();
        }else {
            for ($ini = 0 ; $ini < $this->schcntall ; $ini++){
                $this->setdata["DELCHK"][$ini] = VARS::post("DELCHK".$ini);
                $this->setdata["SCHREGNO2"][$ini] = VARS::post("SCHREGNO2".$ini);
                $this->setdata["NAME"][$ini] = VARS::post("NAME".$ini);
                $this->setdata["T_TIME"][$ini] = VARS::post("T_TIME".$ini);
                $this->setdata["REMARK2"][$ini] = VARS::post("REMARK2".$ini);
            }
        }

        if ($this->field["GRADE"]) 
            $this->field["GRADE"] = sprintf("%02d",$this->field["GRADE"]);
    }

    //入力項目チェック
    function validateInput()
    {
        if (VARS::request("cmd") == 'add'){
            if (!$this->field["SCHREGNO"]) {
                $this->setWarning("学籍番号を入力して下さい。");
                return false;
            }
            //講座チェック用データ取得
            $db = Query::dbCheckOut();
            $query = knjm410Query::getCheckoutch($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!$row){
                $this->setWarning("登録されていない講座です");
                return false;
            }
            Query::dbCheckIn($db);

            //生徒チェック用データ取得
            $db = Query::dbCheckOut();
            $query = knjm410Query::getCheckoutsc($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!$row){
                $this->setWarning("講座に登録されていない学籍番号です");
                return false;
            }
            Query::dbCheckIn($db);

            //重複チェック用データ取得
            $db = Query::dbCheckOut();
            $query = knjm410Query::getCheckout($this);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($row){
                $this->setWarning("既に登録済みです");
                return false;
            }
            Query::dbCheckIn($db);

            if (strlen($this->field["REMARK"]) > 60) {
                $this->setWarning("MSG915",strlen($this->field["REMARK"])."(備考は60バイトまでです)");
                return false;
            }
        }
        return true;
    }

    // 編集完了用のモデルを返します
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            if (VARS::request("cmd") == 'add'){
                $this->insert();
            }else if(VARS::request("cmd") == 'alldel'){
                $this->delete();
            }else {
                $this->delback = array();
                $delc = 0;
                for ($i = 0;$i < $this->schcntall;$i++){
                    if ($this->setdata["DELCHK"][$i]=="on"){
                        $this->delback[$delc] = $this->setdata["SCHREGNO2"][$i];
                        $delc++;
                    }
                }
                $this->schnodel = "";
                for ($i = 0;$i < get_count($this->delback);$i++){
                    if ($i == 0){
                        $this->schnodel .= "(";
                    }else {
                        $this->schnodel .= ",";
                    }
                    $this->schnodel .= "'".$this->delback[$i]."'";
                }
                if (get_count($this->delback) != 0){
                    $this->schnodel .= ")";
                }else {
                    $this->setWarning("削除データを選択して下さい。");
                    return false;
                }
                $this->deletesitei();
            }

        }
    }

    /**
     * インサート時の内部メソッド
     */
    function &insert()
    {
        knjm410Query::getInsertQuery($this);
        knjm410Query::getUpdateQuery($this);
    }
    /**
     * デリート時の内部メソッド
     */
    function &delete()
    {
        knjm410Query::getDeleteQuery($this);
    }
    function &deletesitei()
    {
        knjm410Query::getDeleteQuerysitei($this);
    }

}
?>

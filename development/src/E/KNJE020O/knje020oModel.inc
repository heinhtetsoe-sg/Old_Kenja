<?php

require_once('for_php7.php');

class knje020oModel extends Model {
    var $cmd;
    var $schregno;    //primary
    var $grade;
    var $hr_class;
    var $name;
    var $attendno;
    var $field = array();
    var $check;
    var $annual;                    //学年(年次)
    var $mode;                      //在籍卒業モード
    var $grd_year;                  //卒業年度
    var $grd_semester;              //卒業学期
    var $exp_year;       //左フレームから送られてくる年度
    var $exp_semester;   //左フレームから送られてくる学期

    var $schoolCd;
    var $getPrgId;  //コールされたか
    var $sendAuth;  //親画面の権限
    var $auth;      //権限

    function knje020oModel()
    {
        $this->year = CTRL_YEAR;

        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

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

        $this->getPropertiesAll(); //プロパティファイル

        $this->cmd = VARS::request("cmd");

        //左フレームから送られてくる年度
        if (VARS::get("EXP_YEAR")) {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        //左フレームから送られてくる学期
        if (VARS::get("EXP_SEMESTER")) {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }
        if(VARS::request("SCHREGNO") !=""){
            $this->schregno        = VARS::request("SCHREGNO");
        }
        if(VARS::request("GRADE") !=""){
            $this->grade           = VARS::request("GRADE");
        }
        if(VARS::get("NAME")!=""){
            $this->name            = VARS::get("NAME");
            $this->attendno        = VARS::get("ATTENDNO");
        }
        if (VARS::post("ANNUAL")){   //学年（年次）
            list($tmp["YEAR"], $tmp["ANNUAL"]) = explode(",", VARS::post("ANNUAL"));
            $this->annual = $tmp;
        }

        $this->field = array("JOBHUNT_REC"          =>  VARS::post("jobhunt_rec"),           //就職用特別活動記録
                             "JOBHUNT_ABSENCE"      =>  VARS::post("jobhunt_absence"),       //就職用欠席理由
                             "JOBHUNT_HEALTHREMARK" =>  VARS::post("jobhunt_healthremark"),  //就職用身体状況備考
                             "JOBHUNT_RECOMMEND"    =>  VARS::post("jobhunt_recommend"),     //就職用推薦事由
                             "UPDATED"              =>  VARS::post("updated"));              //更新日付
        $this->check = VARS::post("CHECK");

        //在籍卒業モード
        if (VARS::get("mode")){
            $this->mode = VARS::get("mode");             //在籍卒業モード
        }
        if (VARS::get("GRD_YEAR")){
            $this->grd_year = VARS::get("GRD_YEAR");           //卒業年度
        }
        if (VARS::get("GRD_SEMESTER")){
            $this->grd_semester = VARS::get("GRD_SEMESTER");           //卒業学期
        }

       //生徒一覧を更新
        if (VARS::get("init")){
            unset($this->schregno);     //学籍番号
            unset($this->name);         //生徒氏名
            unset($this->attendno);     //出席番号
        }

    }

    function &getProgrampathModel()
    {
        $db = Query::dbCheckOut();
        $programpath = $db->getOne(knje020oQuery::getProgrampath());
        Query::dbCheckIn($db);

        return $programpath;
    }

    //入力項目チェック
    function validateInput()
    {
        //文字数チェック(改行も含む)
        if(strlen($this->field["JOBHUNT_REC"]) > 494){
            $this->setWarning("MSG914", "(特別活動の記録) 494バイトまで");
            return false;
        }elseif (strlen($this->field["JOBHUNT_ABSENCE"]) >126 ) {
            $this->setWarning("MSG914", "(欠席の主な理由) 126バイトまで");
            return false;
        }elseif(strlen($this->field["JOBHUNT_HEALTHREMARK"]) > 130){
            $this->setWarning("MSG914","(身体状況備考) 130バイトまで");
            return false;
        }elseif(strlen($this->field["JOBHUNT_RECOMMEND"]) > 1158){
            $this->setWarning("MSG914","(本人の長所・推薦事由等) > 1158");
            return false;
        }
        //更新日付チェック
        if($this->cmd =="update"){
            $db = Query::dbCheckOut();
            $query = knje020oQuery::getReportRemark_dat($this);
            $Row = $db->getRow($query,DB_FETCHMODE_ASSOC);
            if($this->field["UPDATED"] != $Row["UPDATED"]){
                $this->setWarning("MSG307");
                return false;
            }
            Query::dbCheckIn($db);

        //更新時にリストから選択しているかをチェック
            if($this->schregno ==""){
                $this->setWarning("MSG304");
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
            knje020oQuery::getUpdateQuery($this);
            $this->setMessage("MSG201");
        }
    }

}
?>

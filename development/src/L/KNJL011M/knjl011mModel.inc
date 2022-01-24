<?php

require_once('for_php7.php');

class knjl011mModel extends Model {

    var $cmd;
    var $year;              //入試年度
    var $semester;          //現在処理学期
    var $examno;            //受験番号
    var $checkexam;
    var $field = array();           //フィールドデータ

    function knjl011mModel()
    {
    }

    function init()
    {
        //起動時に初期化
        if(VARS::get("PROGRAMID")) {
            unset($this->examno);
            unset($this->year);
            unset($this->checkexam);
        }

        $this->cmd = VARS::request("cmd");
        $this->year = CTRL_YEAR + 1;
        $this->semester     = CTRL_SEMESTER;
        $this->examno       = VARS::post("EXAMNO");

        if (strlen($this->examno)) {
            $this->examno = sprintf("%05d",$this->examno);
        }
        
        //フィールド
        $this->field = array("NAME"             =>  VARS::post("NAME"),               //氏名(志願者)
                             "NAME_KANA"        =>  VARS::post("NAME_KANA"),          //フリガナ(志願者)
                             "WNAME"            =>  VARS::post("WNAME"),              //生年月日元号・和暦名
                             "ERACD"            =>  VARS::post("ERACD"),              //生年月日元号
                             "BIRTH_Y"          =>  VARS::post("BIRTH_Y"),            //生年月日年
                             "BIRTH_M"          =>  VARS::post("BIRTH_M"),            //生年月日月
                             "BIRTH_D"          =>  VARS::post("BIRTH_D"),            //生年月日日
                             "BIRTHDAY"         =>  VARS::post("BIRTHDAY"),           //生年月日（西暦）
                             "FS_CD"            =>  VARS::post("FS_CD"),              //出身学校コード
                             "FS_NAME"          =>  VARS::post("FS_NAME"),            //出身学校名
                             "FS_NATPUBPRIDIV"  =>  VARS::post("FS_NATPUBPRIDIV"),    //国公立区分
                             "FS_AREA_DIV"      =>  VARS::post("FS_AREA_DIV"),        //所在地区分
                             "FS_AREA_CD"       =>  VARS::post("FS_AREA_CD"),         //所在地
                             "REMARK1"          =>  VARS::post("REMARK1"),            //所在地
                             "GNAME"            =>  VARS::post("GNAME"),              //氏名(保護者)
                             "GKANA"            =>  VARS::post("GKANA")               //フリガナ(保護者)
                            );

        if ($this->field["FS_AREA_DIV"] != '99' && $this->field["FS_AREA_CD"] != '99') {
            $this->field["REMARK1"] = '';
        }
    }


    //入力項目チェック
    function validateInput()
    {
        //空文字チェック
        if (!($this->examno)) {
            $this->setWarning("MSG301", "( 受験番号 )");
            return false;
        }
        if (!($this->field["NAME"])) {
            $this->setWarning("MSG301", "( 志願者 氏名 )");
            return false;
        }
        if (!($this->field["NAME_KANA"])) {
            $this->setWarning("MSG301", "( 志願者 フリガナ )");
            return false;
        }
        if (!($this->field["FS_NAME"])) {
            $this->setWarning("MSG301", "( 出身学校名 )");
            return false;
        }
        if (!($this->field["FS_NATPUBPRIDIV"])) {
            $this->setWarning("MSG301", "( 国公立区分 )");
            return false;
        }
        if (!($this->field["FS_AREA_DIV"])) {
            $this->setWarning("MSG301", "( 所在地区分 )");
            return false;
        }
        if (!($this->field["FS_AREA_CD"])) {
            $this->setWarning("MSG301", "( 所在地 )");
            return false;
        }
        //キー値変更チェック
        if ($this->cmd != "add" && $this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }
        //日付チェック
        if (strlen($this->field["ERACD"]) || strlen($this->field["BIRTH_Y"]) ||
            strlen($this->field["BIRTH_M"]) || strlen($this->field["BIRTH_D"]))
        {
            $day = knjl011mQuery::CheckDate($this);
            $yy = ((int)$day["NAMESPARE1"]) + ((int)$this->field["BIRTH_Y"] - 1);
            $mm = (int)$this->field["BIRTH_M"];
            $dd = (int)$this->field["BIRTH_D"];
            //日付として正しいかチェック
            if(!checkdate($mm, $dd, $yy)) {
                $this->setWarning("MSG901", "日付を正しく入力して下さい。( 生年月日 )");
                return false;
            }
            $check_date = $yy.sprintf("%02d",$mm).sprintf("%02d",$dd);
            $start_date = str_replace("/", "", $day["NAMESPARE2"]);
            $last_date = str_replace("/", "", $day["NAMESPARE3"]);
            if ((int)$check_date < (int)$start_date || (int)$check_date > (int)$last_date) {
                $this->setWarning("MSG901", "日付を正しく入力して下さい。( 生年月日 )");
                return false;
            }
        }
        //存在チェック
        $row = knjl011mQuery::Existdata($this);
        if (isset($row["EXAMNO"]) && $this->cmd == "add") {
            $this->setWarning("MSG302");
            return false;
        }
        if (!isset($row["EXAMNO"]) && ($this->cmd == "update" || $this->cmd == "back" || $this->cmd == "next")) {
            $this->setWarning("MSG303");
            return false;
        }
        //文字数チェック
        if (strlen($this->examno) > 5) {
            $this->setWarning("MSG915","受験番号は5バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME"]) > 60) {
            $this->setWarning("MSG915","志願者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["NAME_KANA"]) > 120) {
            $this->setWarning("MSG915","志願者( フリガナ )は120バイトまでです。");
            return false;
        }
        if (strlen($this->field["ERACD"]) > 1) {
            $this->setWarning("MSG915","生年月日元号は1バイトまでです。");
            return false;
        }
        if (strlen($this->field["BIRTH_Y"]) > 2) {
            $this->setWarning("MSG915","生年月日年は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["BIRTH_M"]) > 2) {
            $this->setWarning("MSG915","生年月日月は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["BIRTH_D"]) > 2) {
            $this->setWarning("MSG915","生年月日日は2バイトまでです。");
            return false;
        }
        if (strlen($this->field["FS_CD"]) > 7) {
            $this->setWarning("MSG915","出身学校コードは7バイトまでです。");
            return false;
        }
        if (strlen($this->field["FS_NAME"]) > 45) {
            $this->setWarning("MSG915","出身学校名は45バイトまでです。");
            return false;
        }
        if (strlen($this->field["REMARK1"]) > 60) {
            $this->setWarning("MSG915","所在地区分は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["GNAME"]) > 60) {
            $this->setWarning("MSG915","保護者( 氏名 )は60バイトまでです。");
            return false;
        }
        if (strlen($this->field["GKANA"]) > 120) {
            $this->setWarning("MSG915","保護者( フリガナ )は120バイトまでです。");
            return false;
        }

        return true;
    }

    //追加処理
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()){
            $result = knjl011mQuery::getInsertUpdateQuery($this);
            $this->setMessage("MSG201");
            //次の受験番号
            $examno = (int)$this->examno + 1;
            if (strlen($examno) && (int)$examno <= 999) {
                $this->examno = sprintf("%05d",$examno);
            }
        }
        return;
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $result = knjl011mQuery::getInsertUpdateQuery($this);
            if ($this->cmd == "update") {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //削除処理
    function &getDeleteModel()
    {
        if ($this->examno == "") {
            $this->setWarning("MSG304", "( 受験番号 )");
            return false;
        }
        $row = knjl011mQuery::Existdata($this);
        if (!isset($row["EXAMNO"])) {
            $this->setWarning("MSG303");
            return false;
        }
        if ($this->examno != $this->checkexam) {
            $this->setWarning("MSG300", "更新、削除は検索を実行してから行ってください。");
            return false;
        }        
        knjl011mQuery::getDeleteQuery($this);
        $this->examno = "";
        $this->setMessage("MSG201");
    }
 
    //追加処理
    function &getMaxExamno()
    {
        //最後の受験番号＋１
        $this->examno = knjl011mQuery::getMaxExamnoQuery($this->year);
        if (strlen($this->examno)) {
            $this->examno = sprintf("%05d",$this->examno);
        }
        return;
    }

}
?>
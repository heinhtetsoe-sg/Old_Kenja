<?php

require_once('for_php7.php');

class knjf150cModel extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade;
    var $hr_class;
    var $attendno;
    var $field = array();

    var $visit_date;
    var $visit_hour;
    var $visit_minute;
    var $type;
    var $schoolName;
    var $sendPrgId;
    var $sendSubmit;

    var $control_data = array();    //コントロールマスタデータを保持

    var $zipPass;

    function knjf150cModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
    }

    function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
            unset($this->visit_date);
            unset($this->visit_hour);
            unset($this->visit_minute);
            unset($this->type);
            unset($this->sendPrgId);
            unset($this->sendSubmit);

            //学校を判断
            $db = Query::dbCheckOut();
            $this->schoolName = $db->getOne(knjf150cQuery::getSchoolName());
            Query::dbCheckIn($db);

        }
        //デフォルト時間
        $timestamp = time();
        $this->timestamp = date("H-i", $timestamp);
        list($this->hour, $this->minute) = explode("-", $this->timestamp);

        //時間コンボ用
        $this->hourArray = array();
        for ($i = 0; $i < 24; $i++) {
            $this->hourArray[] = array('label' => sprintf("%02d", $i),
                                       'value' => sprintf("%02d", $i));
        }
        //分コンボ用
        $this->minutesArray = array();
        for ($i = 0; $i < 60; $i++) {
            $this->minutesArray[] = array('label' => sprintf("%02d", $i),
                                          'value' => sprintf("%02d", $i));
        }
        //体温整数コンボ用
        $this->BodyHeat1Array = array();
        for ($i = 34; $i < 41; $i++) {
            $this->BodyHeat1Array[] = array('label' => sprintf("%02d", $i),
                                            'value' => sprintf("%02d", $i));
        }
        //体温少数コンボ用
        $this->BodyHeat2Array = array();
        for ($i = 0; $i < 10; $i++) {
            $this->BodyHeat2Array[] = array('label' => strval($i),
                                            'value' => strval($i));
        }

        $this->selectDetailSeq = array();
        $this->selectDetailSeq["01"] = array(1, 2, 3, 4, 5);
        $this->selectDetailSeq["02"] = array(1, "_L1");
        $this->selectDetailSeq["03"] = array(1, 2, 3, 4);
        $this->selectDetailSeq["04"] = array(1, 2);
        $this->selectDetailSeq["05"] = array(1);
        $this->selectDetailSeq["06"] = array(1, 2, 3, 4, 5, 6, 7, 8);
        $this->selectDetailSeq["07"] = array(1, 2, 3, 4, 5, 6, 7, 8);
        $this->selectDetailSeq["08"] = array(1, 2, 3, 4, 5, 6, 7, 8);
        $this->selectDetailSeq["09"] = array(1, 2, 3, 4, 5, 6, 7, 8, 9);
        $this->selectDetailSeq["97"] = array(1, 3, 4);
        $this->selectDetailSeq["98"] = array(1, 2);
        $this->selectDetailSeq["99"] = array("_L1");

        $this->checked  = VARS::request("CHECKED");     //チェック

        $this->field = array();
        foreach ($this->selectDetailSeq as $seq => $remarkArray) {
            foreach ($remarkArray as $remarkName) {
                $getPostName = "SEQ{$seq}_REMARK{$remarkName}";
                $this->field[$getPostName] = VARS::post($getPostName);
            }
        }

        if($this->type == "1"){
            //SEQ004_REMARK1(食事チェック)を再設定
            $seq04_remark1 = VARS::post("SEQ04_REMARK11");
            if($seq04_remark1 == "") $seq04_remark1 = VARS::post("SEQ04_REMARK12");
            $this->field["SEQ04_REMARK1"] = $seq04_remark1;

            //SEQ005_REMARK1(排便チェック)を再設定
            $seq05_remark1 = VARS::post("SEQ05_REMARK11");
            if($seq05_remark1 == "") $seq05_remark1 = VARS::post("SEQ05_REMARK12");
            if($seq05_remark1 == "") $seq05_remark1 = VARS::post("SEQ05_REMARK13");
            if($seq05_remark1 == "") $seq05_remark1 = VARS::post("SEQ05_REMARK14");
            $this->field["SEQ05_REMARK1"] = $seq05_remark1;
        }

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name = VARS::get("NAME");
        }
        if (VARS::get("GRADE") != "") {
            $this->grade = VARS::get("GRADE");
        }
        if (VARS::get("HR_CLASS") != "") {
            $this->hr_class = VARS::get("HR_CLASS");
        }
        if (VARS::get("ATTENDNO") != "") {
            $this->attendno = VARS::get("ATTENDNO");
        }
        if (VARS::get("VISIT_DATE") != "") {
            $this->visit_date = VARS::get("VISIT_DATE");
        }
        if (VARS::get("VISIT_HOUR") != "") {
            $this->visit_hour = VARS::get("VISIT_HOUR");
        }
        if (VARS::get("VISIT_MINUTE") != "") {
            $this->visit_minute = VARS::get("VISIT_MINUTE");
        }
        if (VARS::get("TYPE") != "") {
            $this->type = VARS::get("TYPE");
        }
        if (VARS::get("SEND_PRGID")) {
            $this->sendPrgId = VARS::get("SEND_PRGID");
        }
        if (VARS::get("SEND_SUBMIT")) {
            $this->sendSubmit = VARS::get("SEND_SUBMIT");
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->hr_class);
            unset($this->attendno);
            unset($this->visit_date);
            unset($this->visit_hour);
            unset($this->visit_minute);
            unset($this->type);
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //生徒項目名切替処理
        $schName = "";
        $db = Query::dbCheckOut();
        //テーブルの有無チェック
        $query = knjf150cQuery::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && ($this->schregno || $this->Properties["use_prg_schoolkind"] == "1")) {
            //生徒項目名取得
            $schName = $db->getOne(knjf150cQuery::getSchName($this));
        } elseif ($table_cnt > 0 && ($this->schregno || ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""))) {
            //生徒項目名取得
            $schName = $db->getOne(knjf150cQuery::getSchName($this));
        }
        $this->sch_label = (strlen($schName) > 0) ? $schName : '生徒';
        Query::dbCheckIn($db);

        $this->zipPass = VARS::request("ZIP_PASS");
        $this->file = VARS::file("FILE");
    }

    //削除用のモデルを返します
    function &getDeleteModel()
    {
        if (!isset($this->schregno)) {
            $this->setWarning("MSG308");
            return;
        }

        knjf150cQuery::getDeleteQuery($this);
        $this->setMessage("MSG201");
        $this->field = array();

        return;
    }

    //入力項目チェック
    function validateInput($flg)
    {
        $this->field["SEQ01_REMARK1"] = str_replace("/", "-", $this->field["SEQ01_REMARK1"]);
        $this->field["SEQ01_REMARK2"] = sprintf("%02d", $this->field["SEQ01_REMARK2"]);
        $this->field["SEQ01_REMARK3"] = sprintf("%02d", $this->field["SEQ01_REMARK3"]);
        $this->field["SEQ98_REMARK1"] = ($this->field["SEQ98_REMARK1"]) ? sprintf("%02d", $this->field["SEQ98_REMARK1"]) : "";
        $this->field["SEQ98_REMARK2"] = ($this->field["SEQ98_REMARK2"]) ? sprintf("%02d", $this->field["SEQ98_REMARK2"]) : "";

        $count = get_count(knjf150cQuery::getExistCheck($this));
        if(($flg == "update") && (($this->visit_date != $this->field["SEQ01_REMARK1"]) || 
                                  ($this->visit_hour != $this->field["SEQ01_REMARK2"]) || 
                                  ($this->visit_minute != $this->field["SEQ01_REMARK3"]))){
            $this->setWarning("来室日時が変更されています。");
            return false;
        }
        if(($flg == "update") && !$count){
            $this->setWarning("対象データがありません。");
            return false;
        }
        if(($flg == "insert") && $count){
            $this->setWarning("MSG302");
            return false;
        }

        //桁数チェック
        foreach ($this->errorCheck as $key => $val) {
            if (strlen($this->field[$key]) > $val["LEN"]) {
                $this->setWarning("MSG915","({$val["LABEL"]}は{$val["LEN"]}バイトまでです)");
                return false;
            }
        }
        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel($flg)
    {
        $this->cmd = "subform".$this->type."A";

        if (!$this->validateInput($flg)) {
            return;
        }

        knjf150cQuery::getUpdateQuery($this, $this->field, $flg);
        //ファイルアップロード
        if ($this->schregno && $this->field["SEQ01_REMARK1"] && $this->field["SEQ01_REMARK2"] && $this->field["SEQ01_REMARK3"]) {
            //ファイルパス取得
            $this->filePath = knjf150cModel::getFilePath($this->schregno, $this->type, $this->field);

            if ($this->file['name']) {
                $this->getFileExecModel($this->cmd);
            }
        }

        $this->setMessage("MSG201");

        $this->visit_date = $this->field["SEQ01_REMARK1"];
        $this->visit_hour = $this->field["SEQ01_REMARK2"];
        $this->visit_minute = $this->field["SEQ01_REMARK3"];
        $this->field["VISIT_HOUR"] = $this->field["SEQ01_REMARK2"];
        $this->field["VISIT_MINUTE"] = $this->field["SEQ01_REMARK3"];

        return;
    }

    //ファイルパス取得、作成
    function getFilePath($schregno, $type, $row) {
        //アップロードフォルダ作成
        $dirname  = DOCUMENTROOT . "/nurseoff";
        //ディレクトリ（nurseoff）なければ作成
        if (!is_dir($dirname)) {
            if (!mkdir($dirname)) {
                //作成失敗
            }
        }
        $dirnameSch  = $dirname . "/" . $schregno;
        //ディレクトリ（学籍番号）なければ作成
        if (!is_dir($dirnameSch)) {
            if (!mkdir($dirnameSch)) {
                //作成失敗
            }
        }
        $dirnameFull  = $dirnameSch . "/" . $type."_".$row["SEQ01_REMARK1"].$row["SEQ01_REMARK2"].$row["SEQ01_REMARK3"];
        //ディレクトリ（学籍番号）なければ作成
        if (!is_dir($dirnameFull)) {
            if (!mkdir($dirnameFull)) {
                //作成失敗
            }
        }
        return $dirnameFull;
    }

    /**
     * 実行モデルを返します
    */
    function &getFileExecModel($cmd = "")
    {
        $setFileName = explode(".", $this->file['name']);
        $setFileName[0] = mb_convert_encoding($setFileName[0], "SJIS-win", "UTF-8");
        $filename = $this->filePath."/" . $setFileName[0].'.'.$setFileName[get_count($setFileName) - 1];

        if (!is_uploaded_file($this->file['tmp_name'])) {
            $this->warning = "ファイル名が不正です。";
        } else {
            if (move_uploaded_file($this->file['tmp_name'], $filename) == FALSE){
                $this->warning = "取込失敗です。";
            } else {
                $this->message = "取込正常です。";
            }
        }
        if (!$cmd) {
            $this->cmd = (!$this->isDataAri) ? "subform".$this->type : "subform".$this->type."A";
        }
    }

    /**
     * 削除
    */
    function &getFileDeleteModel()
    {
        $setFileName = mb_convert_encoding($this->zipPass, "SJIS-win", "UTF-8");
        $filename = $this->filePath."/". $setFileName;

        system("rm -f '{$filename}'");

        unset($this->zipPass);
        $this->cmd = (!$this->isDataAri) ? "subform".$this->type : "subform".$this->type."A";
        return true;
    }
}
?>

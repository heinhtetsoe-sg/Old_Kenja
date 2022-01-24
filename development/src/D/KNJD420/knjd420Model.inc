<?php

require_once('for_php7.php');
class knjd420Model extends Model {

    var $cmd;
    var $schregno = "";
    var $name = "";
    var $grade = "";
    var $exp_year = "";
    var $exp_semester = "";
    var $testTable;         //テストコンボ使用テーブル
    var $field  = array();
    var $field2 = array();
    var $field2seq = array();
    var $field3 = array();
    var $field4 = array();
    var $control;           //コントロールマスタ
    var $printGakki;
    var $subclasscd;
    var $unitcd;
    var $retprg;
    var $getPrgId;
    var $auth;
    var $sendAuth;
    

    function knjd420Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);

        if (!strlen($this->printGakki)) {
            //印刷学期の初期値設定
            $db = Query::dbCheckOut();
            $this->printGakki = $db->getOne(knjd420Query::getNameMst($this, "D078"));
            Query::dbCheckIn($db);
        }
    }

    function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
            unset($this->exp_year);
            unset($this->exp_semester);
            unset($this->printGakki);
            unset($this->subclasscd);
            unset($this->unitcd);
            unset($this->retprg);
            unset($this->getPrgId);
            unset($this->auth);
            unset($this->sendAuth);
        }

        $this->paternInfo = array();
        $this->paternInfo[1][1][] = array("UPD_FIELD"   => "REMARK1",
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "35",
                                          "GYOU"        => "10"
                                         );
        $this->paternInfo[1][1][] = array("UPD_FIELD"   => "REMARK_VALUE1",
                                          "NAME"        => "ITEM_REMARK3"
                                         );
        $this->paternInfo[0][1][] = array("UPD_FIELD"   => "REMARK",
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "35",
                                          "GYOU"        => "10"
                                         );
        $this->paternInfo[0][1][] = array("UPD_FIELD"   => "REMARK_VALUE",
                                          "NAME"        => "ITEM_REMARK3"
                                         );
        $this->paternInfo[9][1][] = array("UPD_FIELD"   => "VALUE_TEXT",
                                          "NAME"        => "ITEM_REMARK4",
                                          "MOJI"        => "22",
                                          "GYOU"        => "5"
                                         );

        $this->paternInfo[1][2][] = array("UPD_FIELD"   => "REMARK1",
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "23",
                                          "GYOU"        => "10"
                                         );
        $this->paternInfo[0][2][] = array("UPD_FIELD"   => "REMARK",
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "23",
                                          "GYOU"        => "10"
                                         );
        $this->paternInfo[9][2][] = array("UPD_FIELD"   => "PROCEDURE",
                                          "NAME"        => "ITEM_REMARK5",
                                          "MOJI"        => "23",
                                          "GYOU"        => "50"
                                         );
        $this->paternInfo[9][2][] = array("UPD_FIELD"   => "VALUE_TEXT",
                                          "NAME"        => "ITEM_REMARK4",
                                          "MOJI"        => "23",
                                          "GYOU"        => "5"
                                         );

        $this->paternInfo[1][3][] = array("UPD_FIELD"   => "REMARK1",
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "16",
                                          "GYOU"        => "10"
                                         );
        $this->paternInfo[0][3][] = array("UPD_FIELD"   => "REMARK",
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "16",
                                          "GYOU"        => "10"
                                         );
        $this->paternInfo[9][3][] = array("UPD_FIELD"   => "VALUE_TEXT",
                                          "NAME"        => "ITEM_REMARK4",
                                          "MOJI"        => "16",
                                          "GYOU"        => "5"
                                         );

        $this->paternInfo[1][4][] = array("UPD_FIELD"   => "REMARK1",
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "16",
                                          "GYOU"        => "10"
                                         );
        $this->paternInfo[0][4][] = array("UPD_FIELD"   => "REMARK",
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "16",
                                          "GYOU"        => "10"
                                         );
        $this->paternInfo[9][4][] = array("UPD_FIELD"   => "PROCEDURE",
                                          "NAME"        => "ITEM_REMARK5",
                                          "MOJI"        => "19",
                                          "GYOU"        => "50"
                                         );
        $this->paternInfo[9][4][] = array("UPD_FIELD"   => "VALUE_TEXT",
                                          "NAME"        => "ITEM_REMARK4",
                                          "MOJI"        => "19",
                                          "GYOU"        => "5"
                                         );
        $this->paternInfo[9][4][] = array("UPD_FIELD"   => "VALUE_TEXT2",
                                          "NAME"        => "ITEM_REMARK8",
                                          "MOJI"        => "16",
                                          "GYOU"        => "5"
                                         );

        $this->paternInfo[1][5][] = array("UPD_FIELD"   => "REMARK1",
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "35",
                                          "GYOU"        => "10"
                                         );
        $this->paternInfo[1][5][] = array("UPD_FIELD"   => "REMARK_VALUE1",
                                          "NAME"        => "ITEM_REMARK3"
                                         );
        $this->paternInfo[0][5][] = array("UPD_FIELD"   => "REMARK",
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "35",
                                          "GYOU"        => "10"
                                         );
        $this->paternInfo[0][5][] = array("UPD_FIELD"   => "REMARK_VALUE",
                                          "NAME"        => "ITEM_REMARK3"
                                         );

        $this->paternInfo[1][6][] = array("UPD_FIELD"   => "REMARK1",
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "40",
                                          "GYOU"        => "10"
                                         );
        $this->paternInfo[1][6][] = array("UPD_FIELD"   => "REMARK_VALUE1",
                                          "NAME"        => "ITEM_REMARK3"
                                         );
        $this->paternInfo[0][6][] = array("UPD_FIELD"   => "REMARK",
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "40",
                                          "GYOU"        => "10"
                                         );
        $this->paternInfo[0][6][] = array("UPD_FIELD"   => "REMARK_VALUE",
                                          "NAME"        => "ITEM_REMARK3"
                                         );
        $this->paternInfo[9][6][] = array("UPD_FIELD"   => "VALUE",
                                          "NAME"        => "ITEM_REMARK6",
                                          "MOJI"        => "2"
                                         );

        $this->paternInfo[8][7][] = array("UPD_FIELD"   => "YEAR_TARGET",
                                          "NAME"        => "ITEM_REMARK7",
                                          "MOJI"        => "16",
                                          "GYOU"        => "5"
                                         );
        $this->paternInfo[1][7][] = array("UPD_FIELD"   => "REMARK1",
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "16",
                                          "GYOU"        => "10"
                                         );
        $this->paternInfo[1][7][] = array("UPD_FIELD"   => "REMARK_VALUE1",
                                          "NAME"        => "ITEM_REMARK3"
                                         );
        $this->paternInfo[0][7][] = array("UPD_FIELD"   => "REMARK",
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "16",
                                          "GYOU"        => "10"
                                         );
        $this->paternInfo[0][7][] = array("UPD_FIELD"   => "REMARK_VALUE",
                                          "NAME"        => "ITEM_REMARK3"
                                         );
        $this->paternInfo[9][7][] = array("UPD_FIELD"   => "VALUE_TEXT",
                                          "NAME"        => "ITEM_REMARK4",
                                          "MOJI"        => "16",
                                          "GYOU"        => "5"
                                         );

        $this->paternInfo[1][8][] = array("UPD_FIELD"   => "REMARK1",
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "16",
                                          "GYOU"        => "10"
                                         );
        $this->paternInfo[1][8][] = array("UPD_FIELD"   => "REMARK_VALUE1",
                                          "NAME"        => "ITEM_REMARK3"
                                         );
        $this->paternInfo[0][8][] = array("UPD_FIELD"   => "REMARK",
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "16",
                                          "GYOU"        => "10"
                                         );
        $this->paternInfo[0][8][] = array("UPD_FIELD"   => "REMARK_VALUE",
                                          "NAME"        => "ITEM_REMARK3"
                                         );
        $this->paternInfo[9][8][] = array("UPD_FIELD"   => "VALUE_TEXT",
                                          "NAME"        => "ITEM_REMARK4",
                                          "MOJI"        => "16",
                                          "GYOU"        => "5"
                                         );

        $this->paternInfo[1]["A"][] = array("UPD_FIELD"   => "REMARK_VALUE1",
                                          "NAME"        => "ITEM_REMARK2"
                                         );
        $this->paternInfo[1]["A"][] = array("UPD_FIELD"   => "REMARK1",
                                          "NAME"        => "ITEM_REMARK3",
                                          "MOJI"        => "27",
                                          "GYOU"        => "8"
                                         );
        $this->paternInfo[1]["A"][] = array("UPD_FIELD"   => "REMARK2",
                                          "ADD_SEQ"     => 60,
                                          "NAME"        => "ITEM_REMARK4",
                                          "MOJI"        => "25",
                                          "GYOU"        => "8"
                                         );
        $this->paternInfo[0]["A"][] = array("UPD_FIELD"   => "REMARK_VALUE",
                                          "NAME"        => "ITEM_REMARK2"
                                         );
        $this->paternInfo[0]["A"][] = array("UPD_FIELD"   => "REMARK",
                                          "NAME"        => "ITEM_REMARK3",
                                          "MOJI"        => "27",
                                          "GYOU"        => "8"
                                         );
        $this->paternInfo[0]["A"][] = array("UPD_FIELD"   => "REMARK",
                                          "ADD_SEQ"     => 60,
                                          "NAME"        => "ITEM_REMARK4",
                                          "MOJI"        => "25",
                                          "GYOU"        => "8"
                                         );

        $this->paternInfo[1]["B"][] = array("UPD_FIELD"   => "REMARK",
                                          "SEQ"         => 71,
                                          "NAME"        => "ITEM_REMARK1",
                                          "MOJI"        => "20",
                                          "GYOU"        => "45"
                                         );
        $this->paternInfo[1]["B"][] = array("UPD_FIELD"   => "REMARK",
                                          "SEQ"         => 72,
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "22",
                                          "GYOU"        => "45"
                                         );
        $this->paternInfo[1]["B"][] = array("UPD_FIELD"   => "REMARK",
                                          "SEQ"         => 73,
                                          "NAME"        => "ITEM_REMARK3",
                                          "MOJI"        => "32",
                                          "GYOU"        => "45"
                                         );
        $this->paternInfo[0]["B"][] = array("UPD_FIELD"   => "REMARK",
                                          "ADD_SEQ"     => 70, // SEQ=71
                                          "NAME"        => "ITEM_REMARK1",
                                          "MOJI"        => "20",
                                          "GYOU"        => "45"
                                         );
        $this->paternInfo[0]["B"][] = array("UPD_FIELD"   => "REMARK",
                                          "ADD_SEQ"     => 71, // SEQ=72
                                          "NAME"        => "ITEM_REMARK2",
                                          "MOJI"        => "22",
                                          "GYOU"        => "45"
                                         );
        $this->paternInfo[0]["B"][] = array("UPD_FIELD"   => "REMARK",
                                          "ADD_SEQ"     => 72, // SEQ=73
                                          "NAME"        => "ITEM_REMARK3",
                                          "MOJI"        => "32",
                                          "GYOU"        => "45"
                                         );

        if (VARS::post("PRINT_GAKKI")) {
            $this->printGakki   = VARS::post("PRINT_GAKKI");
        }
        $this->subclasscd   = VARS::request("SUBCLASSCD");
        $this->unitcd       = VARS::request("UNITCD");
        $this->unit_aim_div = VARS::post("UNIT_AIM_DIV");
        $this->remark_cnt   = VARS::post("GROUP_REMARK_CNT");

        for ($i=1; $i<=$this->remark_cnt; $i++) {
            $this->field["REMARK".$i]       =  VARS::post("REMARK".$i);
            $this->field["REMARK_VALUE".$i] =  VARS::post("REMARK_VALUE".$i);
            $this->field["REMARK".(60 + $i)]       =  VARS::post("REMARK".(60 + $i));
            $this->field["REMARK_VALUE".(60 + $i)] =  VARS::post("REMARK_VALUE".(60 + $i));
        }
        $this->field["REMARK71"]       =  VARS::post("REMARK71");
        $this->field["REMARK72"]       =  VARS::post("REMARK72");
        $this->field["REMARK73"]       =  VARS::post("REMARK73");
        $this->field["VALUE_TEXT"]      =  VARS::post("VALUE_TEXT");
        $this->field["VALUE_TEXT2"]     =  VARS::post("VALUE_TEXT2");
        $this->field["PROCEDURE"]       =  VARS::post("PROCEDURE");

        $this->field2["REMARK1"]        =  VARS::post("REMARK1");
        $this->field2["REMARK_VALUE1"]  =  VARS::post("REMARK_VALUE1");
        $this->field2["REMARK2"]        =  VARS::post("REMARK2");
        $this->field2seq["REMARK71"]    =  VARS::post("REMARK71");
        $this->field2seq["REMARK72"]    =  VARS::post("REMARK72");
        $this->field2seq["REMARK73"]    =  VARS::post("REMARK73");

        $this->field3["YEAR_TARGET"]    =  VARS::post("YEAR_TARGET");

        $this->field4["VALUE"]          =  VARS::post("VALUE");

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME") != "") {
            $this->name     = VARS::get("NAME");
        }
        if (VARS::get("RETPRGID") != "") {
            $this->retprg = VARS::get("RETPRGID");
        }
        if (VARS::get("GRADE") != "") {
            if ($this->retprg == "KNJD425") {
                $this->grade        = substr(VARS::get("GRADE"), 0, 2);
                $this->gradeHrClass = VARS::get("GRADE");
            } else {
                $this->grade    = VARS::get("GRADE");
            }
        }

        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->grade);
        }
        if (VARS::get("EXP_YEAR") != "") {
            $this->exp_year = VARS::get("EXP_YEAR");
        }
        if (VARS::get("EXP_SEMESTER") != "") {
            $this->exp_semester = VARS::get("EXP_SEMESTER");
        }

        $db = Query::dbCheckOut();
        $this->schoolKind = $db->getOne(knjd420Query::getSchoolKind($this->exp_year, $this->grade));
        if (!strlen($this->printGakki)) {
            //印刷学期の初期値設定
            $this->printGakki = $db->getOne(knjd420Query::getD078($this->exp_year, "D".$this->schoolKind."78", CTRL_SEMESTER));
            if ($this->printGakki == '') {
                $this->printGakki = $db->getOne(knjd420Query::getD078($this->exp_year, "D".$this->schoolKind."78"));
            }
        }
        Query::dbCheckIn($db);
    }

    //入力項目チェック
    function validateInput($cmd)
    {
        //必須チェック
        if ($this->schregno == "") {
            $this->setWarning("MSG304");
            return false;
        }
        if ($this->printGakki == "") {
            $this->setWarning("MSG301", "印刷学期");
            return false;
        }
        if ($this->subclasscd == "") {
            $this->setWarning("MSG301", "科目コード");
            return false;
        }
        if ($this->unit_aim_div == "1") {
            if (strlen($this->unitcd) == 0) {
                $this->setWarning("MSG301", "単元");
                return false;
            }
        }

        //文字数チェック
        if ($cmd == "update") {
            $div_array = array($this->unit_aim_div, "8", "9");
            foreach ($div_array as $div) {
                if ($div == "0") {
                    if (get_count($this->paternInfo[$div][$this->schregInfo["GUIDANCE_PATTERN"]]) > 0) {
                        for ($i=1; $i<=$this->remark_cnt; $i++) {
                            foreach ($this->paternInfo[$div][$this->schregInfo["GUIDANCE_PATTERN"]] as $key => $val) {
                                $seq = $val["ADD_SEQ"] + $i;
                                $upd_field  = $val["UPD_FIELD"].$seq;
                                $value      = $this->field[$upd_field];
                                $itemname   = $this->itemName[$val["NAME"]].$seq;

                                if ($val["GYOU"]) {
                                    if ($val["MOJI"] == "1") {
                                        if ($this->validate_row_cnt($value, ($val["MOJI"] * 2)) > 1) {
                                            $this->setWarning("MSG915","({$itemname}は{$val["MOJI"]}文字までです)");
                                            return false;
                                        }
                                    } else {
                                        //バイト数チェック
                                        if (strlen($value) > 6100) {
                                            $this->setWarning("MSG915");
                                            return false;
                                        }
                                    }
                                } else if ($val["MOJI"]) {
                                    if ($this->validate_row_cnt($value, $val["MOJI"]) > 1) {
                                        $this->setWarning("MSG915","({$itemname}は{$val["MOJI"]}文字までです)");
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (get_count($this->paternInfo[$div][$this->schregInfo["GUIDANCE_PATTERN"]]) > 0) {
                        foreach ($this->paternInfo[$div][$this->schregInfo["GUIDANCE_PATTERN"]] as $key => $val) {
                            $value = ($div == "1") ? $this->field2[$val["UPD_FIELD"]] : (($div == "8") ? $this->field3[$val["UPD_FIELD"]] : $this->field[$val["UPD_FIELD"]]);

                            if ($val["GYOU"]) {
                                if ($div == "9") {
                                    //バイト数チェック
                                    if (strlen($value) > 6100) {
                                        $this->setWarning("MSG915");
                                        return false;
                                    }
                                } else {
                                    if ($this->validate_row_cnt($value, ($val["MOJI"] * 2)) > $val["GYOU"]) {
                                        $this->setWarning("MSG915","({$this->itemName[$val["NAME"]]}は{$val["GYOU"]}行までです)");
                                        return false;
                                    }
                                }
                            } else if ($val["MOJI"]) {
                                if ($this->validate_row_cnt($value, ($val["MOJI"] * 2)) > 1) {
                                    $this->setWarning("MSG915","({$this->itemName[$val["NAME"]]}は{$val["MOJI"]}文字までです)");
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    //更新用のモデルを返します
    function &getUpdateModel()
    {
        if (!$this->validateInput("update")) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
    }
    //アップデート時の内部メソッド
    function &update()
    {
        $db = Query::dbCheckOut();

        if ($this->schregInfo["GUIDANCE_PATTERN"] == "7")  {
            //更新 -- HREPORT_GUIDANCE_SCHREG_YDAT
            knjd420Query::getInsertQuery3($db, $this);
        }
        if ($this->unit_aim_div == "1")  {
            foreach ($this->paternInfo[$this->unit_aim_div][$this->schregInfo["GUIDANCE_PATTERN"]] as $key => $val) {
                //更新 -- HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT
                if ($val["SEQ"]) {
                    $field = $val["UPD_FIELD"].$val["SEQ"];
                    knjd420Query::getInsertQuery2seq($db, $this, $val["SEQ"], $val["UPD_FIELD"], $this->field2seq[$field]);
                }
            }
            //更新 -- HREPORT_GUIDANCE_SCHREG_UNIT_DAT
            knjd420Query::getInsertQuery2($db, $this);
        }

        $div_array = array($this->unit_aim_div, "9");
        foreach ($div_array as $div) {
            if (get_count($this->paternInfo[$div][$this->schregInfo["GUIDANCE_PATTERN"]]) > 0) {
                foreach ($this->paternInfo[$div][$this->schregInfo["GUIDANCE_PATTERN"]] as $key => $val) {
                    if ($val["UPD_FIELD"] == "VALUE") {
                        //更新 -- RECORD_SCORE_DAT
                        knjd420Query::getInsertQuery4($db, $this);
                    } else if ($div == "0") {
                        for ($i=1; $i<=$this->remark_cnt; $i++) {
                            //更新 -- HREPORT_GUIDANCE_SCHREG_DAT
                            $seq = $val["ADD_SEQ"] + $i;
                            $upd = $val["UPD_FIELD"].$seq;
                            knjd420Query::getInsertQuery1($db, $this, $val["UPD_FIELD"], $this->field[$upd], $seq);
                        }
                    } else if ($div == "9") {
                        if ($val["UPD_FIELD"] == "PROCEDURE") {
                            $seq = "51";
                        } else if ($val["UPD_FIELD"] == "VALUE_TEXT") {
                            $seq = "52";
                        } else if ($val["UPD_FIELD"] == "VALUE_TEXT2") {
                            $seq = "53";
                        }
                        //更新 -- HREPORT_GUIDANCE_SCHREG_DAT
                        knjd420Query::getInsertQuery1($db, $this, "REMARK", $this->field[$val["UPD_FIELD"]], $seq);
                    }
                }
            }
        }

        Query::dbCheckIn($db);
        return;
    }

    //削除用のモデルを返します
    function &getDeleteModel() {
        if (!$this->validateInput("delete")) {
            return;
        }
        $db = Query::dbCheckOut();

        if ($this->schregInfo["GUIDANCE_PATTERN"] == "7")  {
            //削除 -- HREPORT_GUIDANCE_SCHREG_YDAT
            knjd420Query::getDeleteQuery3($db, $this);
        }
        if ($this->unit_aim_div == "1")  {
            //削除 -- HREPORT_GUIDANCE_SCHREG_UNIT_DAT
            knjd420Query::getDeleteQuery2($db, $this);
            knjd420Query::getDeleteQuery2Seq($db, $this);
        }

        $div_array = array($this->unit_aim_div, "9");
        foreach ($div_array as $div) {
            if (get_count($this->paternInfo[$div][$this->schregInfo["GUIDANCE_PATTERN"]]) > 0) {
                foreach ($this->paternInfo[$div][$this->schregInfo["GUIDANCE_PATTERN"]] as $key => $val) {
                    if ($val["UPD_FIELD"] == "VALUE") {
                        //削除 -- RECORD_SCORE_DAT
                        knjd420Query::getDeleteQuery4($db, $this);
                    } else if ($div == "0") {
                        for ($i=1; $i<=$this->remark_cnt; $i++) {
                            //削除 -- HREPORT_GUIDANCE_SCHREG_DAT
                            $seq = $val["ADD_SEQ"] + $i;
                            knjd420Query::getDeleteQuery1($db, $this, $seq);
                        }
                    } else if ($div == "9") {
                        if ($val["UPD_FIELD"] == "PROCEDURE") {
                            $seq = "51";
                        } else if ($val["UPD_FIELD"] == "VALUE_TEXT") {
                            $seq = "52";
                        } else if ($val["UPD_FIELD"] == "VALUE_TEXT2") {
                            $seq = "53";
                        }
                        //削除 -- HREPORT_GUIDANCE_SCHREG_DAT
                        knjd420Query::getDeleteQuery1($db, $this, $seq);
                    }
                }
            }
        }

        $this->setMessage("MSG201");
        Query::dbCheckIn($db);
    }

    function getBeforePrintGakki($db) {
        $query = knjd420Query::getD078($this->exp_year, "D".$this->schoolKind."78");
        $result = $db->query($query);
        $before = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["VALUE"] == $this->printGakki) {
                break;
            }
            $before = $row["VALUE"];
        }
        return $before;
 
    }

    //コピー処理
    function &getCopyModel() {
        if ($this->validateInput("copy")) {
            $db = Query::dbCheckOut();
            $key = "";
            //前年度のデータ取得
            $beforePrintGakki = $this->getBeforePrintGakki($db);


            $query = knjd420Query::copyHreportGuidanceSchregDatBpattern($this, $beforePrintGakki, "COUNT");
            $count = $db->getOne($query);
            $query = knjd420Query::copyHreportGuidanceSchregUnitSeqDatBpattern($this, $beforePrintGakki, "COUNT");
            $countU = $db->getOne($query);
            if ($count == 0 && $countU == 0) {
                $this->setMessage("MSG917", "対象データが存在しません。");
                return false;
            }
            $query = knjd420Query::copyHreportGuidanceSchregDatBpattern($this, $this->printGakki, "COUNT");
            $count2 = $db->getOne($query);
            $query = knjd420Query::copyHreportGuidanceSchregUnitSeqDatBpattern($this, $this->printGakki, "COUNT");
            $countU2 = $db->getOne($query);
            if ($count2 > 0 || $countU2 > 0) {
                $this->setMessage("MSG917", "データが存在します。");
                return false;
            }

            $query = knjd420Query::copyHreportGuidanceSchregDatBpattern($this, $beforePrintGakki);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー    -- HREPORT_GUIDANCE_SCHREG_DAT
                $query = knjd420Query::getInsertHreportGuidanceSchregDatBpatternQuery($row, $this->printGakki); 
                $db->query($query);
            }
            $result->free();

            $query = knjd420Query::copyHreportGuidanceSchregUnitSeqDatBpattern($this, $beforePrintGakki);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //コピー    -- HREPORT_GUIDANCE_SCHREG_UNIT_SEQ_DAT
                $query = knjd420Query::getInsertHreportGuidanceSchregUnitSeqDatBpatternQuery($row, $this->printGakki); 
                $db->query($query);
            }
            $result->free();
            Query::dbCheckIn($db);
            $this->setMessage("MSG201");
        }
        return true;
    }
}
?>

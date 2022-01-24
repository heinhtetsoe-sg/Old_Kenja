<?php

require_once('for_php7.php');

class knje372bModel extends Model {
    var $field      = array();
    var $cmd;
    var $oyear;
    var $ryear;

    function knje372bModel()
    {
    }

    function init()
    {
        $this->cmd      = VARS::request("cmd");
        $this->ryear    = (VARS::request("RYEAR")) ? VARS::request("RYEAR") : $this->ryear;
        $this->oyear    = (VARS::request("OYEAR")) ? VARS::request("OYEAR") : $this->oyear;

        if (VARS::get("RECOMMENDATION_CD")) {
            $this->recommendation_cd    = VARS::get("RECOMMENDATION_CD");
            $this->depSCd               = VARS::get("DEPARTMENT_S");
            $this->depHCd               = VARS::get("DEPARTMENT_H");
            $this->dispOrder            = VARS::get("DISP_ORDER");
            $this->listOrder            = VARS::get("DEPARTMENT_LIST_ORDER");
        } else if (in_array($this->cmd, array("edit", "copy", "changeOyear")) &&
                   !isset($this->warning) && !VARS::get("clear")) {
            unset($this->recommendation_cd);
        }
        $this->field = array(
                             "RECOMMENDATION_CD"     => (VARS::post("RECOMMENDATION_CD") != "") ? sprintf("%04d", VARS::post("RECOMMENDATION_CD")) : "",         //※推薦枠番号
                             "DEPARTMENT_S"          => VARS::post("DEPARTMENT_S"),                                                                              //専願学科番号
                             "DEPARTMENT_H"          => VARS::post("DEPARTMENT_H"),                                                                              //併願学科番号
                             "DISP_ORDER"            => (VARS::post("DISP_ORDER") != "") ? sprintf("%02d", VARS::post("DISP_ORDER")) : "",                       //表示順
                             "FACULTY_NAME"          => VARS::post("FACULTY_NAME"),                                                                              //学部
                             "FACULTY_ABBV"          => VARS::post("FACULTY_ABBV"),                                                                              //学部略称
                             "DEPARTMENT_NAME"       => VARS::post("DEPARTMENT_NAME"),                                                                           //学科
                             "DEPARTMENT_ABBV"       => VARS::post("DEPARTMENT_ABBV"),                                                                           //学科略称
                             "DEPARTMENT_ABBV2"      => VARS::post("DEPARTMENT_ABBV2"),                                                                          //学科略称2
                             "LIMIT_COUNT"           => VARS::post("LIMIT_COUNT"),                                                                               //学科枠数
                             "LIMIT_COUNT_S"         => VARS::post("LIMIT_COUNT_S"),                                                                             //専願枠数
                             "LIMIT_COUNT_H"         => VARS::post("LIMIT_COUNT_H"),                                                                             //併願枠数
                             "WITHOUT_H_FLG"         => VARS::post("WITHOUT_H_FLG"),                                                                             //併願有無
                             "DEPARTMENT_LIST_CD"    => VARS::post("DEPARTMENT_LIST_CD"),                                                                        //学科コード
                             "DEPARTMENT_LIST_ORDER" => (VARS::post("DEPARTMENT_LIST_ORDER") != "") ? sprintf("%02d", VARS::post("DEPARTMENT_LIST_ORDER")) : "", //出力順
                             "FACULTY_LIST_NAME"     => VARS::post("FACULTY_LIST_NAME"),                                                                         //学部
                             "DEPARTMENT_LIST_NAME"  => VARS::post("DEPARTMENT_LIST_NAME"),                                                                      //学科（専攻）
                             "SCHOOL_CD"             => (VARS::post("SCHOOL_CD") != "") ? sprintf("%08d", VARS::post("SCHOOL_CD")) : "",                         //賢者学校コード(大学コード)
                             "FACULTYCD"             => (VARS::post("FACULTYCD") != "") ? sprintf("%03d", VARS::post("FACULTYCD")) : "",                         //賢者学校コード(学部コード)
                             "DEPARTMENTCD"          => (VARS::post("DEPARTMENTCD") != "") ? sprintf("%03d", VARS::post("DEPARTMENTCD")) : ""                    //賢者学校コード(学科コード)
                             );

        //初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->ryear);
            unset($this->oyear);
        }
    }

    //データコピー
    function &getCopyModel()
    {
        if (!$this->copyCheck()) {
            return;
        }
        knje372bQuery::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }

    //コピーチェック
    function copyCheck()
    {
        //DB接続
        $db = Query::dbCheckOut();

        //参照年度・入試年度チェック     
        if ($this->ryear == $this->oyear) {
            $this->setWarning("MSG203", "同年度のコピーは出来ません。");
            return false;
        }
        //入試年度のデータ存在チェック
        if (0 < $db->getOne(knje372bQuery::getList($this, "CNT"))) {
            $this->setWarning("対象年度のデータが既に存在しています。");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);

        return true;
    }

    //入力項目チェック
    function validateInput()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $check = array("RECOMMENDATION_CD" => "推薦枠番号");

        $checkArray["FACULTY_NAME"]       = array("TITLE" => "学部",                 "LEN" => "30");
        $checkArray["FACULTY_ABBV"]       = array("TITLE" => "学部略称",             "LEN" => "30");
        $checkArray["DEPARTMENT_NAME"]    = array("TITLE" => "学科",                 "LEN" => "60");
        $checkArray["DEPARTMENT_ABBV"]    = array("TITLE" => "学科略称",             "LEN" => "30");
        $checkArray["DEPARTMENT_ABBV2"]   = array("TITLE" => "学科略称2",            "LEN" => "9");
        $checkArray["DEPARTMENT_LIST_CD"] = array("TITLE" => "推薦名簿(学科コード)", "LEN" => "9");

        //キーのチェック
        foreach ($this->field as $key => $val) {
            if ($check[$key] && trim($val) == "") {
                $this->setWarning("MSG301","(".$check[$key].")");
                return false;
            }
        }

        //１レコード取得
        $row = $db->getRow(knje372bQuery::getList($this, "CHECK"));

        //文字数チェック
        if($this->cmd == "add" || $this->cmd == "update"){
            foreach ($checkArray as $fieldName => $fieldVal) {
                if (strlen($this->field[$fieldName]) > $fieldVal["LEN"]) {
                    $this->setWarning("MSG901", "( ".$fieldVal["TITLE"]." ) ".$fieldVal["LEN"]."バイトまで");
                    return false;
                }
            }
        }

        if ($this->cmd == "add") {
            //重複チェック
            if (is_array($row)) {
                $this->setWarning("MSG918", "(推薦枠番号)");
                return false;
            }
        } else {
            //キーの更新チェック
            if ($this->recommendation_cd == "") {
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            if ($this->recommendation_cd != $this->field["RECOMMENDATION_CD"]){
                $this->setWarning("MSG308","キー値は変更できません");
                return false;
            }
            //存在チェック
            if (!is_array($row)) {
                $this->setWarning("MSG303", "(推薦枠番号)");
                return false;
            }
        }

        //画面で専願併願に同じ学科番号を入力した場合
        if ($this->field["DEPARTMENT_H"] != "" && $this->field["DEPARTMENT_S"] != "" && $this->field["DEPARTMENT_H"] == $this->field["DEPARTMENT_S"]) {
            $this->setWarning("MSG901", "「専願・併願学科番号」が一致しています。");
            return false;
        }
        $chkDepSCdFlg = $this->cmd == "add" || ($this->cmd == "update" && $this->depSCd != $this->field["DEPARTMENT_S"]) ? true : false ;
        //重複チェック(専願学科番号)
        if ($chkDepSCdFlg && $db->getOne(knje372bQuery::getExistsDepCd($this, "DEPARTMENT_S")) > 0) {
            $this->setWarning("MSG901", "「専願学科番号」が重複しています。");
            return false;
        }
        $chkDepHCdFlg = $this->cmd == "add" || ($this->cmd == "update" && $this->depHCd != $this->field["DEPARTMENT_H"]) ? true : false ;
        //重複チェック(併願学科番号)
        if ($chkDepHCdFlg && $db->getOne(knje372bQuery::getExistsDepCd($this, "DEPARTMENT_H")) > 0) {
            $this->setWarning("MSG901", "「併願学科番号」が重複しています。");
            return false;
        }

        $chgDispOrderFlg = $this->cmd == "add" || ($this->cmd == "update" && $this->dispOrder != $this->field["DISP_ORDER"]) ? true : false ;
        //重複チェック(表示)
        if ($chgDispOrderFlg && $db->getOne(knje372bQuery::getExistsOrder($this, "DISP_ORDER")) > 0) {
            $this->setWarning("MSG901", "「表示順」が重複しています。");
            return false;
        }
        $chgListOrderFlg = $this->cmd == "add" || ($this->cmd == "update" && $this->listOrder != $this->field["DEPARTMENT_LIST_ORDER"]) ? true : false ;
        //重複チェック(出力順)
        if ($chgListOrderFlg && $db->getOne(knje372bQuery::getExistsOrder($this, "DEPARTMENT_LIST_ORDER")) > 0) {
            $this->setWarning("MSG901", "「出力順」が重複しています。");
            return false;
        }

        //DB切断
        Query::dbCheckIn($db);

        return true;
    }

    //INSERT
    function &getInsertModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->recommendation_cd = $this->field["RECOMMENDATION_CD"];
            $this->setMessage("MSG201");
        }
        return;
    }

    //UPDATE
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            $this->update();
            $this->recommendation_cd = $this->field["RECOMMENDATION_CD"];
            $this->setMessage("MSG201");
        }
        return;
    }
    function &update()
    {
        $result = knje372bQuery::getUpdateQuery($this);
        return $result;
    }


    //DELETE
    function &getDeleteModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            knje372bQuery::getDeleteQuery($db, $this);
            $this->recommendation_cd = "";
            $this->setMessage("MSG201");
        }
        return;
    }
}
?>

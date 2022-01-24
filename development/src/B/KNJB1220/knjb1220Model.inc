<?php

require_once('for_php7.php');


class knjb1220Model extends Model {
    
    var $field = array();
    var $cmd;
    var $exe_year;
    var $rirekiCode;
    var $applicantno;
    var $schregno;
    var $pattern_cd;
    var $addchk;
    var $kounin;
    var $disp;
    var $newStudent;
    var $sougouMajor;
    var $isSougou;
    var $info;

    function knjb1220Model()
    {
        $this->field = array();
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->pattern_cd = VARS::post("PATTERN_CD");
        $this->exe_year = VARS::post("EXE_YEAR");
        $this->rirekiCode = VARS::post("RIREKI_CODE");
        $this->disp = VARS::post("DISP");
        $this->grdYotei = VARS::post("GRD_YOTEI");

        if (VARS::get("SCHREGNO")) {
            $this->schregno   = VARS::get("SCHREGNO");
            $this->search_div = VARS::get("SEARCH_DIV");
            $this->hrClassHyoujiFlg = VARS::get("HR_CLASS_HYOUJI_FLG");
        }

        $this->addchk = array();
        $this->kounin = array();
        $this->zoutan = array();
        if ($this->cmd == "") {
            unset($this->exe_year);
            unset($this->rirekiCode);
            unset($this->schregno);
            unset($this->disp);
            unset($this->addchk);
            unset($this->kounin);
            unset($this->zoutan);
        } else if ($this->cmd == "update" || $this->cmd == "updateNext" ||
                   $this->cmd == "updatePrev" || $this->cmd == "select") {
            $this->addchk = VARS::request("ADDCHK");
            if (!is_array($this->addchk) && VARS::request("ADDCHK")) {
                $this->addchk[0] = VARS::request("ADDCHK");
            }
            $this->kounin = VARS::request("KOUNIN");
            if (!is_array($this->kounin) && VARS::request("KOUNIN")) {
                $this->kounin[0] = VARS::request("KOUNIN");
            }
            if (get_count($this->subclassArray) > 0) {
                foreach ($this->subclassArray as $key => $val) {
                    $this->zoutan[$val] = VARS::request("ZOUTAN".$val);
                }
            }
        }
        $this->disp = $this->cmd == "pattern" ? "2" : $this->disp;

        $this->getPropertiesAll(); //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //更新処理
    function &getUpdateModel()
    {
        if ($this->rirekiCode == "") {
            $this->setWarning("MSG203", "履修登録日が選択されていません。");
            return false;
        }
        $this->update();
        if ($this->cmd == "updateNext") {
            $this->cmd = "next";
        } else if ($this->cmd == "updatePrev") {
            $this->cmd = "prev";
        } else {
            $this->cmd = "main";
        }

        $this->setMessage("MSG201");

        unset($this->addchk);
        unset($this->kounin);
        unset($this->grdYotei);
    }

    //更新処理
    function update()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjb1220Query::getSeme($this);
        $result = $db->query($query);
        $semeArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semeArray[] = $row;
        }
        $result->free();

        //履修登録
        $notDelInState = "(";
        $sep = "";
        for ($i = 0; $i < get_count($this->addchk); $i++) {
            $addvalue = preg_split("/:/", $this->addchk[$i]);
            foreach ($semeArray as $key => $semeData) {
                $query = knjb1220Query::insertSubclassStd($db, $this, $semeData, $addvalue);
                if ($query) {
                    $db->query($query);
                }
                $setDataKey = $semeData["SEMESTER"].$addvalue[1].$addvalue[2].$addvalue[0].$addvalue[3];
                $notDelInState .= $sep."'{$setDataKey}'";
                $sep = ",";
            }
        }
        $notDelInState = $sep ? $notDelInState.")" : "('')";
        $db->query(knjb1220Query::deleteSubclassStd($this, $notDelInState));

        //高認登録
        $db->query(knjb1220Query::deleteKounin($this));
        for ($i = 0; $i < get_count($this->kounin); $i++) {
            $addvalue = preg_split("/:/", $this->kounin[$i]);
            $query = knjb1220Query::insertKounin($db, $this, $addvalue);
            $db->query($query);
        }

        //増単
        if (get_count($this->zoutan) > 0) {
            foreach ($this->zoutan as $key => $val) {
                $addvalue = preg_split("/:/", $key);
                if (strlen($val) > 0) {
                    $query = knjb1220Query::insertZoutan($db, $this, $addvalue, $val);
                    $db->query($query);
                }
            }
        }

        //卒業予定
        $query = knjb1220Query::insUpdSchregBaseYearDetail($db, $this);
        $db->query($query);

        //生徒パターン
        if ($this->pattern_cd) {
            $query = knjb1220Query::delInsPaternStd($db, $this);
            $db->query($query);
        }

        $query = knjb1220Query::getSchregNo($this);
        $setSchregNo = $db->getOne($query);
        $this->schregno = $setSchregNo;

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
    }

}
?>

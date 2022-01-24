<?php

require_once('for_php7.php');

class knjb0011Model extends Model {
    var $cmd;
    var $field = array();
    var $reset = array();
    var $appointed_day;
    var $year;
    var $rirekiCode;

    function knjb0011Model() {
        $this->field = array();
    }

    function init() {
        $this->cmd = VARS::request("cmd");
        $this->field = $_POST;
        $this->selectdata = VARS::post("selectdata");
        //年度
        $this->year = VARS::request("YEAR");
        //学期
        $this->semester = VARS::request("SEMESTER");
        //コースコード
        $this->course = VARS::request("COURSE");
        //教科コード
        $this->selectClass = VARS::request("selectClass");
        //選択GROUP
        $this->selectGroup = VARS::request("SELECT_GROUP");
        //群コード
        $this->schregNo = VARS::request("SCHREGNO");
        //開始日
        $this->appDate = VARS::request("APPDATE");
        //講座数(チェックボックス添え字)
        $this->chaircnt = VARS::request("CHAIRCNT");
        //学期終了日
        $this->setEndDate = VARS::request("SETENDDATE");
        //学期開始日
        $this->setStartDate = VARS::request("SETSTARTDATE");
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /* 入力項目チェック */
    function validateInput() {
        $db = Query::dbCheckOut();

        //名簿に登録された講座取得
        $this->regd_chaircd = $this->regd_chrAppdate = array();
        $result = $db->query(knjb0011Query::getChaircd($this->year, $this->semester, $this->schregNo));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->regd_chaircd[] = $row["CHAIRCD"];
            $this->regd_chrAppdate[$row["CHAIRCD"]] = $row["APPDATE"];
        }
        $result->free();

        //日付の変更可能範囲取得
        $this->chgOkDate =  array();
        $result = $db->query(knjb0011Query::getDate($this->year, $this->semester, $this->schregNo));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->chgOkDate[$row["CHAIRCD"]] = array($row["SDATE"], $row["EDATE"]);
        }
        $result->free();

        //群コード・講座コード一覧取得
        $this->chrList = $this->flg_chk = $this->flg_rgd = array();
        $query = knjb0011Query::getChairName($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->chrList[$row["CHAIRCD"]] = $row["CHAIRABBV"];
            //チェックボックス  1:ON 0:OFF
            $this->flg_chk[$row["CHAIRCD"]] = (in_array($row["CHAIRCD"], $this->updChair)) ? "1" : "0";
            //講座名簿  1:登録済み 0:未登録
            $this->flg_rgd[$row["CHAIRCD"]] = (in_array($row["CHAIRCD"], $this->regd_chaircd)) ? "1" : "0";
        }
        $result->free();

        //日付の範囲チェック
        foreach ($this->chrList as $chrCd => $chrName) {
            //チェックボックスが変更された講座を処理(ON => OFF / OFF => ON)
            if (($this->flg_chk[$chrCd] == "1" && $this->flg_rgd[$chrCd] != "1") || ($this->flg_chk[$chrCd] != "1" && $this->flg_rgd[$chrCd] == "1")) {
                //日付の変更可能範囲取得
                list ($sdate, $edate) = $this->chgOkDate[$chrCd];
                $sdate = str_replace("-", "/", $sdate);
                $edate = str_replace("-", "/", $edate);

                if ($sdate > $this->appDate || $this->appDate > $edate) {
                    $this->setWarning("MSG901", "（".$chrName."） 日付変更可能範囲：".$sdate."～".$edate);
                    return false;
                }
            }
        }

        Query::dbCheckIn($db);
        return true;
    }

    /* 更新処理 */
    function &getUpdateModel() {
        $this->updChair = array();
        for ($cCnt = 0; $cCnt < $this->chaircnt; $cCnt++) {
            $setChair = VARS::post("CHAIR{$cCnt}");
            if ($setChair) {
                $this->updChair[] = $setChair;
            }
        }
        if ($this->validateInput()) {
            //更新
            $cnt = knjb0011Query::getUpdateQuery($this);
            if ($cnt == 0) {
                $this->setWarning("MSG202","変更されたデータはありません。");
                $this->selectdata = $this->updChair;
            } else {
                $this->setMessage("MSG201");
                unset($this->selectdata);
            }
        } else {
            $this->selectdata = $this->updChair;
        }
        return;
    }
}
?>

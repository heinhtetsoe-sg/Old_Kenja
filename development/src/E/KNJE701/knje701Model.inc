<?php

require_once('for_php7.php');

class knje701Model extends Model
{
    public $field = array();
    public $cmd;
    public $heigancd;
    public $heigangname;
    public $facultycd;
    public $departmentcd;
    public $checkFlg = true;
    public $ctrl;

    public function knje701Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->ctrl);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->heigancd);
            unset($this->heigangname);
            unset($this->facultycd);
            unset($this->departmentcd);
            $this->checkFlg = true;
        }

        $this->cmd = VARS::request("cmd");

        $this->field = array("HEIGANCD"      => VARS::post("HEIGANCD"),
                             "HEIGANGNAME"   => VARS::post("HEIGANGNAME"),
                             "FACULITYCD"    => VARS::post("FACULITYCD"),
                             "DEPARTMENTCD"  => VARS::post("DEPARTMENTCD"),
                             "selectleft"    => VARS::post("selectleft"),
                             "selectleftval" => VARS::post("selectleftval"));

        //リンクをクリックしたとき
        if (VARS::get("HEIGAN_CD")) {
            $this->heigancd = VARS::get("HEIGAN_CD");
        }
        if (VARS::get("HEIGAN_GROUPNAME")) {
            $this->heigangname = VARS::get("HEIGAN_GROUPNAME");
        }
        if (VARS::get("FACULITYCD")) {
            $this->facultycd = VARS::get("FACULITYCD");
        }
        if (VARS::get("DEPARTMENTCD")) {
            $this->departmentcd = VARS::get("DEPARTMENTCD");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    public function validateInput($kind)
    {
        if ($kind == "insert" || $kind == "update") {
            //必須チェック
            if ($this->field["FACULITYCD"] == "") {
                $this->setWarning("MSG301", "( 学部コード )");
                return false;
            }
            if ($this->field["selectleft"] == "") {
                $this->setWarning("MSG301", "( 学科コード )");
                return false;
            }
            if ($this->field["HEIGANCD"] == "") {
                $this->setWarning("MSG301", "( 併願コード )");
                return false;
            }
            if ($this->field["HEIGANGNAME"] == "") {
                $this->setWarning("MSG301", "( 併願グループ名称 )");
                return false;
            }

            //文字数チェック
            if (strlen($this->field["HEIGANGNAME"]) > 9) {
                $this->setWarning("MSG915", "(併願グループ名称IDは3文字までです。)");
                return false;
            }
        }
        if ($kind == "insert") {
            $db = Query::dbCheckOut();
            //併願コードでの重複チェック
            $rowCnt = $db->getOne(knje701Query::duplicateCheckHdat($this));
            if ($rowCnt != "0") {
                //併願コードデータが存在するとき
                $this->setWarning("MSG302");
                return false;
            }
            //併願名称での重複チェック
            $rowCnt = $db->getOne(knje701Query::duplicateCheckHdat($this, "1"));
            if ($rowCnt != "0") {
                //併願名称データが存在するとき
                $this->setWarning("MSG302");
                return false;
            }
            //学科での重複チェック
            $rowCnt = $db->getOne(knje701Query::duplicateCheckDat($this));
            if ($rowCnt != "0") {
                //学科データが存在するとき
                $this->setWarning("MSG302");
                return false;
            }
            Query::dbCheckIn($db);
        }
        if ($kind == "update") {
            $db = Query::dbCheckOut();
            //併願コードでの重複チェック
            $rowCnt = $db->getOne(knje701Query::duplicateCheckHdat($this));
            if ($this->field["HEIGANCD"] != $this->heigancd &&
                $rowCnt != "0") {
                //併願コードを変更し、かつ既にデータが存在するとき
                $this->setWarning("MSG302");
                return false;
            }
            //併願名称での重複チェック
            $rowCnt = $db->getOne(knje701Query::duplicateCheckHdat($this, "1"));
            if ($this->field["HEIGANGNAME"] != $this->heigangname &&
                $rowCnt != "0") {
                //併願名称を変更し、かつ既にデータが存在するとき
                $this->setWarning("MSG302");
                return false;
            }
            //学科名称での重複チェック
            $rowCnt = $db->getOne(knje701Query::duplicateCheckDat($this));
            if ($this->field["selectleft"] != $this->departmentcd &&
                $this->field["FACULITYCD"]  != $this->facultycd   &&
                $rowCnt != "0") {
                //学科名称を変更し、かつ既にデータが存在するとき
                $this->setWarning("MSG302");
                return false;
            }
            Query::dbCheckIn($db);
        }
        return true;
    }


    //追加用のモデルを返します
    public function getInsertModel()
    {
        //入力チェック
        if ($this->validateInput("insert")) {
            $this->insert();
            if (!isset($this->warning)) {
                $this->setMessage("MSG201");
                $this->heigancd     = $this->field["HEIGANCD"];
                $this->heigangname  = $this->field["HEIGANGNAME"];
                $this->facultycd    = $this->field["FACULITYCD"];
                $this->departmentcd = str_replace(",", "-", $this->field["selectleft"]);
                $this->field["FACULITYCD"] = null;
            }
        } else {
            $this->checkFlg = false;
        }
    }

    //追加
    public function insert()
    {
        $db = Query::dbCheckOut();
        $db->query(knje701Query::getInsertQueryHdat($this));
        $gakka = explode(",", $this->field["selectleft"]);
        foreach ($gakka as $val) {
            $db->query(knje701Query::getInsertQueryDat($this, $val));
        }
        Query::dbCheckIn($db);
    }

    //更新用のモデルを返します
    public function getUpdateModel()
    {
        //データを選択していないとき
        if (!isset($this->heigancd)    ||
            !isset($this->heigangname) ||
            !isset($this->facultycd)) {
            $this->setWarning("MSG308");
            $this->checkFlg = false;
            return false;
        }
        //入力チェック
        if ($this->validateInput("update")) {
            $this->update();
            if (!isset($this->warning)) {
                $this->setMessage("MSG201");
                $this->heigancd     = $this->field["HEIGANCD"];
                $this->heigangname  = $this->field["HEIGANGNAME"];
                $this->facultycd    = $this->field["FACULITYCD"];
                $this->departmentcd = str_replace(",", "-", $this->field["selectleft"]);
                $this->field["FACULITYCD"] = null;
            }
        } else {
            $this->checkFlg = false;
        }
    }

    //更新
    public function update()
    {
        if ($this->validateInput("update")) {
            $db = Query::dbCheckOut();
            //削除して追加(INT_RECOMMENDATION_HEIGAN_HDAT)
            $db->query(knje701Query::getDeleteQueryHdat($this, "1"));
            if ($this->$facultycd != $this->field["FACULITYCD"]) {
                $db->query(knje701Query::getDeleteQueryHdat($this, "2"));
            }
            $db->query(knje701Query::getInsertQueryHdat($this));

            //削除して追加(INT_RECOMMENDATION_HEIGAN_DAT)
            $db->query(knje701Query::getDeleteQueryDat($this, "1"));
            if ($this->$facultycd != $this->field["FACULITYCD"]) {
                $db->query(knje701Query::getDeleteQueryDat($this, "2"));
            }
            //選択された学科を配列に格納
            $gakka = explode(",", $this->field["selectleft"]);
            foreach ($gakka as $val) {
                $db->query(knje701Query::getInsertQueryDat($this, $val));
            }

            Query::dbCheckIn($db);
        }
    }

    //前年度コピー
    public function getCopyModel()
    {
        $db = Query::dbCheckOut();

        //今年度のデータ取得(INT_RECOMMENDATION_HEIGAN_HDAT)
        $result = $db->query(knje701Query::existenceCheck("INT_RECOMMENDATION_HEIGAN_HDAT", CTRL_YEAR));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this_year_checkH = $row;
        }
        //今年度のデータ取得(INT_RECOMMENDATION_HEIGAN_DAT)
        $result = $db->query(knje701Query::existenceCheck("INT_RECOMMENDATION_HEIGAN_DAT", CTRL_YEAR));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this_year_checkD = $row;
        }
        //前年度のデータ取得(INT_RECOMMENDATION_HEIGAN_HDAT)
        $result = $db->query(knje701Query::existenceCheck("INT_RECOMMENDATION_HEIGAN_HDAT", CTRL_YEAR - 1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $last_year_checkH = $row;
        }
        //今年度のデータ取得(INT_RECOMMENDATION_HEIGAN_DAT)
        $result = $db->query(knje701Query::existenceCheck("INT_RECOMMENDATION_HEIGAN_DAT", CTRL_YEAR - 1));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $last_year_checkD = $row;
        }

        //データの存在チェック
        if ($this_year_checkH["CNT"] != "0" || $this_year_checkD["CNT"] != "0") {
            //今年度のデータが存在するとき
            $this->setWarning("既にデータが存在します。");
            return false;
        } elseif (($last_year_checkH["CNT"] == "0") || ($last_year_checkD["CNT"] == "0")) {
            //前年度のデータが存在しないとき
            $this->setWarning("MSG303");
            return false;
        } else {
            //前年度からのコピー(INT_RECOMMENDATION_HEIGAN_HDAT)
            $result = $db->query(knje701Query::getHdatCopyDate(CTRL_YEAR - 1));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $db->query(knje701Query::copyInsertHdat($row));//前年度のデータを今年度のデータとしてINSERT
            }
            //前年度からのコピー(INT_RECOMMENDATION_HEIGAN_DAT)
            $result = $db->query(knje701Query::getDatCopyDate(CTRL_YEAR - 1));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $db->query(knje701Query::copyInsertDat($row));//前年度のデータを今年度のデータとしてINSERT
            }
        }
        Query::dbCheckIn($db);
    }

    //削除
    public function getDeleteModel()
    {
        //データを選択していないとき
        if (!isset($this->heigancd)    ||
            !isset($this->heigangname) ||
            !isset($this->facultycd)) {
            $this->setWarning("MSG308");
            $this->checkFlg = false;
            return false;
        }
        $db = Query::dbCheckOut();
        $db->query(knje701Query::getDeleteQueryHdat($this, "2"));
        $db->query(knje701Query::getDeleteQueryDat($this, "2"));
        Query::dbCheckIn($db);
        if (!isset($this->warning)) {
            $this->setMessage("データを削除しました。");
            //初期化
            unset($this->heigancd);
            unset($this->heigangname);
            unset($this->facultycd);
            unset($this->departmentcd);
            $this->field["HEIGANCD"]    = "";
            $this->field["HEIGANGNAME"] = "";
            $this->field["FACULITYCD"]  = null;
            $this->checkFlg = true;
        }
    }
}

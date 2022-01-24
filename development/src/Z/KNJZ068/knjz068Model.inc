<?php

require_once('for_php7.php');
class knjz068Model extends Model
{
    public $field = array();
    public $cmd;
    public $ibyear;
    public $ibgrade;
    public $ibclasscd;
    public $ibprg_course;
    public $ibcurriculum_cd;
    public $ibsubclasscd;
    public $ibeval_div1;
    public $ibeval_div2;
    public $ibeval_mark;
    public $lastColumn;//CSVの最後の列の値

    public function knjz068Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->ibyear);
            unset($this->ibgrade);
            unset($this->ibclasscd);
            unset($this->ibprg_course);
            unset($this->ibcurriculum_cd);
            unset($this->ibsubclasscd);
            unset($this->ibeval_div1);
            unset($this->ibeval_div2);
            unset($this->ibeval_mark);
        }

        list($ibclasscd, $ibprg_course, $ibcurriculum_cd, $ibsubclasscd) = explode('-', VARS::post("IBSUBCLASS"));

        $this->field = array("IBCLASSCD"        =>  $ibclasscd,
                             "IBPRG_COURSE"     =>  $ibprg_course,
                             "IBCURRICULUM_CD"  =>  $ibcurriculum_cd,
                             "IBSUBCLASSCD"     =>  $ibsubclasscd,
                             "IBEVAL_DIV1"      =>  VARS::post("IBEVAL_DIV1"),
                             "IBEVAL_DIV2"      =>  VARS::post("IBEVAL_DIV2"),
                             "IBEVAL_MARK"      =>  VARS::post("IBEVAL_MARK"),
                             "IBEVAL_NAME"      =>  VARS::post("IBEVAL_NAME"),
                             "IBEVAL_ABBV"      =>  VARS::post("IBEVAL_ABBV"),
                             "IBEVAL_NAME_ENG"  =>  VARS::post("IBEVAL_NAME_ENG"),
                             "IBEVAL_ABBV_ENG"  =>  VARS::post("IBEVAL_ABBV_ENG"),
                             "IBSORT"           =>  VARS::post("IBSORT"),
                             "IBPERFECT"        =>  VARS::post("IBPERFECT"),
                             );

        $this->cmd = VARS::request("cmd");

        $db = Query::dbCheckOut();
        //年度（初期値）
        $this->ibyear = ($this->ibyear == "") ? CTRL_YEAR : $this->ibyear;
        //学年（初期値）
        $this->ibgrade = ($this->ibgrade == "") ? $db->getOne(knjz068Query::getIBGrade($this, "list")) : $this->ibgrade;
        //コース（初期値）
        $this->ibprg_course = ($this->ibprg_course == "") ? $db->getOne(knjz068Query::getIBPrgCourse($this, "list")) : $this->ibprg_course;
        Query::dbCheckIn($db);

        if (VARS::post("IBYEAR") != "") {
            $this->ibyear = VARS::post("IBYEAR");
        }
        if (VARS::post("IBGRADE") != "") {
            $this->ibgrade = VARS::post("IBGRADE");
        }
        if (VARS::post("IBPRG_COURSE") != "") {
            $this->ibprg_course = VARS::post("IBPRG_COURSE");
        }

        //左フレームからのパラメータ
        if (VARS::get("IBCLASSCD")) {
            $this->ibclasscd = VARS::get("IBCLASSCD");
        }
        if (VARS::get("IBCURRICULUM_CD")) {
            $this->ibcurriculum_cd = VARS::get("IBCURRICULUM_CD");
        }
        if (VARS::get("IBSUBCLASSCD")) {
            $this->ibsubclasscd = VARS::get("IBSUBCLASSCD");
        }
        if (VARS::get("IBEVAL_DIV1") != "") {
            $this->ibeval_div1 = VARS::get("IBEVAL_DIV1");
        }
        if (VARS::get("IBEVAL_DIV2") != "") {
            $this->ibeval_div2 = VARS::get("IBEVAL_DIV2");
        }
        if (VARS::get("IBEVAL_MARK")) {
            $this->ibeval_mark = VARS::get("IBEVAL_MARK");
        }

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        //必須チェック
        if (trim($this->ibyear) == "") {
            $this->setWarning("MSG301", "(年度)");
            return false;
        }
        if (trim($this->ibgrade) == "") {
            $this->setWarning("MSG301", "(学年)");
            return false;
        }
        if (trim($this->ibprg_course) == "") {
            $this->setWarning("MSG301", "(IBコース)");
            return false;
        }
        if (trim($this->field["IBCLASSCD"]) == "" || trim($this->field["IBCURRICULUM_CD"]) == "" || trim($this->field["IBSUBCLASSCD"]) == "") {
            $this->setWarning("MSG301", "(科目)");
            return false;
        }
        if (trim($this->field["IBEVAL_DIV1"]) == "") {
            $this->setWarning("MSG301", "(評価区分1)");
            return false;
        }
        if (trim($this->field["IBEVAL_DIV2"]) == "") {
            $this->setWarning("MSG301", "(評価区分2)");
            return false;
        }
        if (trim($this->field["IBEVAL_MARK"]) == "") {
            $this->setWarning("MSG301", "(評価規準記号)");
            return false;
        }

        if ($this->cmd == "delete") {
            //キーの更新チェック
            if ($this->ibclasscd != $this->field["IBCLASSCD"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->ibprg_course != $this->field["IBPRG_COURSE"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->ibcurriculum_cd != $this->field["IBCURRICULUM_CD"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->ibsubclasscd != $this->field["IBSUBCLASSCD"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->ibeval_div1 != $this->field["IBEVAL_DIV1"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->ibeval_div2 != $this->field["IBEVAL_DIV2"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if ($this->ibeval_mark != $this->field["IBEVAL_MARK"]) {
                $this->setWarning("MSG308");
                return false;
            }
            if (!strlen($this->ibclasscd) || !strlen($this->ibgrade) || !strlen($this->ibcurriculum_cd) || !strlen($this->ibsubclasscd) || !strlen($this->ibeval_div1) || !strlen($this->ibeval_div2) || !strlen($this->ibeval_mark)) {
                $this->setWarning("MSG308");
                return false;
            }
        } else {
            //必須チェック
            if (trim($this->field["IBEVAL_NAME"]) == "") {
                $this->setWarning("MSG301", "(評価規準名称)");
                return false;
            }
            if (trim($this->field["IBEVAL_NAME_ENG"]) == "") {
                $this->setWarning("MSG301", "(評価規準名称英字)");
                return false;
            }
            if (trim($this->field["IBPERFECT"]) == "") {
                $this->setWarning("MSG301", "(満点)");
                return false;
            }

            //文字数チェック
            if (strlen($this->field["IBEVAL_MARK"]) > 2) {
                $this->setWarning("MSG915", "(評価規準記号は2バイトまでです)");
                return false;
            } elseif (strlen($this->field["IBEVAL_NAME"]) > 90) {
                $this->setWarning("MSG915", "(評価規準名称は90バイトまでです)");
                return false;
            } elseif (strlen($this->field["IBEVAL_ABBV"]) > 90) {
                $this->setWarning("MSG915", "(評価規準略称は90バイトまでです)");
                return false;
            } elseif (strlen($this->field["IBEVAL_NAME_ENG"]) > 90) {
                $this->setWarning("MSG915", "(評価規準名称英字は90バイトまでです)");
                return false;
            } elseif (strlen($this->field["IBEVAL_ABBV_ENG"]) > 90) {
                $this->setWarning("MSG915", "(評価規準略称英字は90バイトまでです)");
                return false;
            } elseif (strlen($this->field["IBSORT"]) > 2) {
                $this->setWarning("MSG915", "(表示順は2バイトまでです)");
                return false;
            } elseif (strlen($this->field["IBPERFECT"]) > 2) {
                $this->setWarning("MSG915", "(満点は2バイトまでです)");
                return false;
            }
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz068Query::getIBViewNameYmst($this->ibyear, $this->ibgrade, $this->field["IBCLASSCD"], $this->field["IBPRG_COURSE"], $this->field["IBCURRICULUM_CD"], $this->field["IBSUBCLASSCD"], $this->field["IBEVAL_DIV1"], $this->field["IBEVAL_DIV2"], $this->field["IBEVAL_MARK"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        //対象がなければエラー
        if ($this->cmd == "delete" && !is_array($row)) {
            $this->setWarning("MSG303");
            return false;
        }

        return true;
    }

    /**********/
    /* INSERT */
    /**********/
    public function &getInsertModel()
    {
        if ($this->validateInput()) {
            //登録
            knjz068Query::getInsertQuery($this);
            $this->setMessage("MSG201");

            $this->ibclasscd        = $this->field["IBCLASSCD"];
            $this->ibprg_course     = $this->field["IBPRG_COURSE"];
            $this->ibcurriculum_cd  = $this->field["IBCURRICULUM_CD"];
            $this->ibsubclasscd     = $this->field["IBSUBCLASSCD"];
            $this->ibeval_div1      = $this->field["IBEVAL_DIV1"];
            $this->ibeval_div2      = $this->field["IBEVAL_DIV2"];
            $this->ibeval_mark      = $this->field["IBEVAL_MARK"];
        }
    }

    /**********/
    /* DELETE */
    /**********/
    public function &getDeleteModel()
    {
        if ($this->validateInput()) {
            //削除
            knjz068Query::getDeleteQuery($this);
            $this->setMessage("MSG201");

            unset($this->ibclasscd);
            unset($this->ibcurriculum_cd);
            unset($this->ibsubclasscd);
            unset($this->ibeval_div1);
            unset($this->ibeval_div2);
            unset($this->ibeval_mark);
            unset($this->field);
        }
    }

    /********/
    /* COPY */
    /********/
    public function &getCopyModel()
    {
        $db = Query::dbCheckOut();

        //存在チェック（前年度）
        $pre_year = $this->ibyear - 1;
        $cnt_pre = $db->getOne(knjz068Query::checkIBViewNameYmst($pre_year));
        if ($cnt_pre == 0) {
            $this->setWarning("MSG917", "前年度のデータが存在しません。");
            return false;
        }
        //存在チェック（対象年度）
        $cnt_this = $db->getOne(knjz068Query::checkIBViewNameYmst($this->ibyear));
        if ($cnt_this > 0) {
            $this->setWarning("MSG917", "対象年度にデータが存在します。");
            return false;
        }

        Query::dbCheckIn($db);

        //コピー
        knjz068Query::getCopyQuery($this);
        $this->setMessage("MSG201");

        return;
    }

    /***********/
    /* CSV出力 */
    /***********/
    public function getCsvModel()
    {
        //ヘッダ項目
        $header = array("GRADE_NAME"            => "学年",
                        "IBSUBCLASSNAME_ENG"    => "IB科目名称英字",
                        "IBSUBCLASSNAME"        => "IB科目名称",
                        "IBEVAL_DIV1_NAME"      => "評価区分1",
                        "IBEVAL_DIV2_NAME"      => "評価区分2",
                        "IBEVAL_MARK"           => "記号",
                        "IBEVAL_NAME_ENG"       => "名称英字",
                        "IBEVAL_NAME"           => "名称",
                        "IBPERFECT"             => "満点",
                        $this->lastColumn       => $this->lastColumn
                        );

        $contents  = "";

        //ヘッダをセット
        $contents .= implode($header, ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //データをセット
        $query = knjz068Query::getCsvQuery($this);
        $result = $db->query($query);
        $data = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //ヘッダ項目のみセット
            foreach ($header as $key => $val) {
                $contents .= $row[$key].",";
            }
            $contents .= "\n";
            $data = true;
        }

        //IBコース名称
        $ibprg_course = $db->getOne(knjz068Query::getIBPrgCourseName($this));

        //DB切断
        Query::dbCheckIn($db);

        if ($data) {
            $couse = $this->ibyear."年度_IB評価規準マスタ（".$ibprg_course."）";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}

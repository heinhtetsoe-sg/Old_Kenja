<?php

require_once('for_php7.php');
class knjz068bModel extends Model
{
    public $field = array();
    public $field2 = array();
    public $cmd;
    public $ibyear;
    public $ibgrade;
    public $ibclasscd;
    public $ibprg_course;
    public $ibcurriculum_cd;
    public $ibsubclasscd;
    public $lastColumn;//CSVの最後の列の値

    public function knjz068bModel()
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

            $this->field = array();
            $this->field2 = array();
        }

        list($ibclasscd, $ibprg_course, $ibcurriculum_cd, $ibsubclasscd) = explode('-', VARS::post("IBSUBCLASS"));

        $this->field = array("IBCLASSCD"        =>  $ibclasscd,
                             "IBPRG_COURSE"     =>  $ibprg_course,
                             "IBCURRICULUM_CD"  =>  $ibcurriculum_cd,
                             "IBSUBCLASSCD"     =>  $ibsubclasscd,
                             "IBSEQ_CNT"        =>  VARS::post("IBSEQ_CNT")
                             );

        if ($this->field["IBSEQ_CNT"] > 0) {
            $this->field2 = array();
            for ($i = 1; $i <= $this->field["IBSEQ_CNT"]; $i++) {
                list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', VARS::post("SUBCLASS_".$i));

                $this->field2["YEAR_".$i]           =  VARS::post("YEAR_".$i);
                $this->field2["SEMESTER_".$i]       =  VARS::post("SEMESTER_".$i);
                $this->field2["GRADE_".$i]          =  VARS::post("GRADE_".$i);
                $this->field2["CLASSCD_".$i]        =  $classcd;
                $this->field2["SCHOOL_KIND_".$i]    =  $school_kind;
                $this->field2["CURRICULUM_CD_".$i]  =  $curriculum_cd;
                $this->field2["SUBCLASSCD_".$i]     =  $subclasscd;
            }
        } else {
            $this->field2 = array();
        }

        $this->cmd = VARS::request("cmd");

        $db = Query::dbCheckOut();
        //年度（初期値）
        $this->ibyear = ($this->ibyear == "") ? CTRL_YEAR : $this->ibyear;
        //学年（初期値）
        $this->ibgrade = ($this->ibgrade == "") ? $db->getOne(knjz068bQuery::getIBGrade($this, "list")) : $this->ibgrade;
        //IBコース（初期値）
        $this->ibprg_course = ($this->ibprg_course == "") ? $db->getOne(knjz068bQuery::getIBPrgCourse($this, "list")) : $this->ibprg_course;
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
            if (!strlen($this->ibclasscd) || !strlen($this->ibprg_course) || !strlen($this->ibcurriculum_cd) || !strlen($this->ibsubclasscd)) {
                $this->setWarning("MSG308");
                return false;
            }
        } else {
            $label1 = ($this->ibprg_course == "M") ? "(Unit数)" : "(Task数)";
            $label2 = ($this->ibprg_course == "M") ? "Unit" : "Task";

            //必須チェック
            if (trim($this->field["IBSEQ_CNT"]) == "") {
                $this->setWarning("MSG301", $label1);
                return false;
            }
            $err_ibseq = "";
            for ($i = 1; $i <= $this->field["IBSEQ_CNT"]; $i++) {
                if (
                    ($this->field2["YEAR_".$i] == "" && $this->field2["SEMESTER_".$i] == "" && $this->field2["GRADE_".$i] == "" && $this->field2["SUBCLASSCD_".$i] == "") ||
                    ($this->field2["YEAR_".$i] != "" && $this->field2["SEMESTER_".$i] != "" && $this->field2["GRADE_".$i] != "" && $this->field2["SUBCLASSCD_".$i] != "")
                ) {
                    //正常
                } else {
                    $sep = ($err_ibseq == "") ? "" : ",";
                    $err_ibseq .= $sep.$label2.$i;
                }
            }
            if ($err_ibseq != "") {
                $err_ibseq = (strlen($err_ibseq) > 40) ? substr($err_ibseq, 0, 40).'...' : $err_ibseq;
                $this->setWarning("MSG301", "(".$err_ibseq.")");
                return false;
            }

            //存在チェック
            $db = Query::dbCheckOut();
            $err_ibsubclass = "";
            for ($i = 1; $i <= $this->field["IBSEQ_CNT"]; $i++) {
                //件数取得（IB科目）
                $cnt_ibsubclass = $db->getOne(knjz068bQuery::getIBSubclasscdCheck($this->field2["YEAR_".$i], $this->field["IBCLASSCD"], $this->field["IBPRG_COURSE"], $this->field["IBCURRICULUM_CD"], $this->field["IBSUBCLASSCD"]));
                if ($cnt_ibsubclass == 0) {
                    $err_ibsubclass = $this->field2["YEAR_".$i];
                }
            }
            Query::dbCheckIn($db);
            //年度のIB科目がなければエラー
            if ($err_ibsubclass != "") {
                $this->setWarning("MSG305", $err_ibsubclass."年度のIB科目が存在していません。");
                return false;
            }
        }

        //存在チェック
        $db = Query::dbCheckOut();
        $query = knjz068bQuery::getIBSubclassUnitDatLinkNo($this->ibyear, $this->ibgrade, $this->field["IBCLASSCD"], $this->field["IBPRG_COURSE"], $this->field["IBCURRICULUM_CD"], $this->field["IBSUBCLASSCD"]);
        $link_no = $db->getOne($query);
        Query::dbCheckIn($db);
        //対象がなければエラー
        if ($this->cmd == "delete" && !$link_no) {
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
            knjz068bQuery::getInsertQuery($this);
            $this->setMessage("MSG201");

            $this->ibclasscd        = $this->field["IBCLASSCD"];
            $this->ibprg_course     = $this->field["IBPRG_COURSE"];
            $this->ibcurriculum_cd  = $this->field["IBCURRICULUM_CD"];
            $this->ibsubclasscd     = $this->field["IBSUBCLASSCD"];

            $this->setCmd("edit");
        } else {
            $this->setCmd("check");
        }
    }

    /**********/
    /* DELETE */
    /**********/
    public function &getDeleteModel()
    {
        if ($this->validateInput()) {
            //削除
            knjz068bQuery::getDeleteQuery($this);
            $this->setMessage("MSG201");

            unset($this->ibclasscd);
            unset($this->ibcurriculum_cd);
            unset($this->ibsubclasscd);
            unset($this->field);
            unset($this->field2);
        }
    }

    /********/
    /* COPY */
    /********/
    public function &getCopyModel()
    {

        //コピー
        $rtnMsg = knjz068bQuery::getCopyQuery($this);
        if ($rtnMsg == "sonzai") {
            $this->setWarning("MSG201", "一部のデータは対象年度にIB科目、科目の\\nいずれかが存在しないためコピーできません。");
        } elseif ($rtnMsg == "choufuku") {
            $this->setWarning("MSG917", "重複エラーです。");
            return false;
        } else {
            $this->setMessage("MSG201");
        }

        return;
    }
    
    /********/
    /* CSV  */
    /********/
    //サーバ ⇒ クライアント
    public function getDownloadCsvModel()
    {
        //ヘッダ、書出
        $header = array("年度",
                        "学年",
                        "IBコース",
                        "IB教科・科目名",
                        "IB科目コード",
                        "",
                        "",
                        "",
                        "Unit/Task",
                        "年度",
                        "履修学期",
                        "学年",
                        "教科・科目名",
                        "科目コード",
                        "",
                        "",
                        "",
                        $this->lastColumn);
        $contents = implode(",", $header) . "\r\n";
        
        $query = knjz068bQuery::getCsvData($this);
        $db = Query::dbCheckOut();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //配列の値を,で連結
            $row = str_replace(",", ".", $row);//データ内にカンマがあればコロンに置換
            $contents .= implode($row, ",") ."\n";
            
            $data = true;
        }
        $result->free();
        Query::dbCheckIn($db);
        if ($data) {
            $couse="IB教科・科目毎Unit数一覧";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}

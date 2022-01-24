<?php

require_once('for_php7.php');

class knjc035fModel extends Model
{
    public $cmd;
    public $field;
    public $appointed_day;
    public $lesson;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限

    public function knjc035fModel()
    {
        $this->field = array();
    }

    public function init()
    {
        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();  //プロパティファイルの読込み

        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId     = VARS::get("SEND_PRGID");
            $this->sendAuth     = VARS::get("SEND_AUTH");
            $this->sendChair    = VARS::get("SEND_CHAIR");
            $this->sendSubclass = VARS::get("SEND_SUBCLASS");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field["CHAIRCD"] = VARS::post("CHAIRCD");

        if ($this->cmd == "schoolkind") {
            $this->field["COURSE_MAJOR"] = "";
            $this->field["SUBCLASSCD"] = "";
            $this->field["CHAIRCD"] = "";
        }
        if ($this->cmd == "course") {
            $this->field["SUBCLASSCD"] = "";
            $this->field["CHAIRCD"] = "";
        }
        if ($this->cmd == "subclasscd") {
            $this->field["CHAIRCD"] = "";
        }

        //校種
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->field["SCHOOL_KIND"] = VARS::request("SCHOOL_KIND");
        } elseif ($this->Properties["useSchool_KindField"] == "1") {
            $this->field["SCHOOL_KIND"] = SCHOOLKIND;
        }

        //課程学科コード
        if ($this->Properties["use_school_detail_gcm_dat"] == "1") {
            $this->field["COURSE_MAJOR"] = VARS::request("COURSE_MAJOR");
        } else {
            $this->field["COURSE_MAJOR"] = '0000';
        }

        //科目コード
        $this->field["SUBCLASSCD"] = VARS::request("SUBCLASSCD");

        //講座コード
        $this->field["CHAIRCD"] = VARS::request("CHAIRCD");

        //コールされていたら、その科目のみ使用可能
        if (VARS::get("SEND_PRGID")) {
            //科目コード
            $this->field["SUBCLASSCD"] = $this->sendSubclass;
            //講座コード
            $this->field["CHAIRCD"] = $this->sendChair;
        }

        //月コード
        $this->field["MONTHCD"] = VARS::request("MONTHCD");

        //編集対象データを取得
        $this->field["SCHREGNO"]        = VARS::post("SCHREGNO");       //学籍番号
        $this->field["APPOINTED_DAY"]   = VARS::post("APPOINTED_DAY");  //締め日
        $this->field["LESSON"]          = VARS::post("LESSON");         //授業日数
        $this->field["OFFDAYS"]         = VARS::post("OFFDAYS");        //休学日数
        $this->field["ABROAD"]          = VARS::post("ABROAD");         //留学日数
        $this->field["ABSENT"]          = VARS::post("ABSENT");         //公欠日数
        $this->field["SUSPEND"]         = VARS::post("SUSPEND");        //出停日数
        $this->field["KOUDOME"]         = VARS::post("KOUDOME");        //交止
        $this->field["VIRUS"]           = VARS::post("VIRUS");          //伝染病
        $this->field["MOURNING"]        = VARS::post("MOURNING");       //忌引日数
        $this->field["SICK"]            = VARS::post("SICK");           //病欠
        $this->field["NOTICE"]          = VARS::post("NOTICE");         //事故欠届
        $this->field["NONOTICE"]        = VARS::post("NONOTICE");       //事故欠無
        $this->field["NURSEOFF"]        = VARS::post("NURSEOFF");       //保健室欠課
        $this->field["LATE"]            = VARS::post("LATE");           //遅刻回数
        $this->field["EARLY"]           = VARS::post("EARLY");          //早退回数

        $this->field["LESSON_SET"]      = VARS::post("LESSON_SET");     //授業時数(セット用)
        $this->field["MOVE_ENTER"]      = VARS::post("MOVE_ENTER");     //エンター押下時の移動方向
        $this->field["EXECUTED"]        = VARS::post("EXECUTED");       //出欠完了

        $this->field["useLine"]      = VARS::post("useLine");

        $this->appointed_day = VARS::post("APPOINTED_DAY");

        if ($this->cmd == "update") {
            if ($this->Properties["use_prg_schoolkind"] == "1") {
                $this->field["SCHOOL_KIND"]     = VARS::post("HIDDEN_SCHOOL_KIND");
            }
            if ($this->Properties["use_school_detail_gcm_dat"] == "1") {
                $this->field["COURSE_MAJOR"]    = VARS::post("HIDDEN_COURSE_MAJOR");
            }
            $this->field["SUBCLASSCD"]      = VARS::post("HIDDEN_SUBCLASSCD");
            $this->field["CHAIRCD"]         = VARS::post("HIDDEN_CHAIRCD");
            $this->field["MONTHCD"]         = VARS::post("HIDDEN_MONTHCD");
            $this->field["LESSON_SET"]      = VARS::post("HIDDEN_LESSON_SET");
            $this->field["MOVE_ENTER"]      = VARS::post("HIDDEN_MOVE_ENTER");
            $this->field["EXECUTED"]        = VARS::post("HIDDEN_EXECUTED");
        }

        list($this->field["MONTH"], $this->field["SEMESTER"]) = explode("-", $this->field["MONTHCD"]);

        /******************/
        /*  出欠項目取得  */
        /******************/
        //出欠項目一覧
        $this->attendItem = array();
        $this->attendItem["LESSON"]     = array("0000", "",     "授業時数");
        $this->attendItem["OFFDAYS"]    = array("A004", "2",    "");
        $this->attendItem["ABROAD"]     = array("A004", "1",    "");
        $this->attendItem["ABSENT"]     = array("C001", "1",    "");
        $this->attendItem["SUSPEND"]    = array("C001", "2",    "");
        if ($this->Properties["useKoudome"] == "true") {
            $this->attendItem["KOUDOME"]    = array("C001", "25",   "");
        }
        if ($this->Properties["useVirus"] == "true") {
            $this->attendItem["VIRUS"]      = array("C001", "19",   "");
        }
        $this->attendItem["MOURNING"]   = array("C001", "3",    "");
        $this->attendItem["SICK"]       = array("C001", "4",    "");
        $this->attendItem["NOTICE"]     = array("C001", "5",    "");
        $this->attendItem["NONOTICE"]   = array("C001", "6",    "");
        $this->attendItem["NURSEOFF"]   = array("C001", "14",   "");
        $this->attendItem["LATE"]       = array("C001", "15",   "");
        $this->attendItem["EARLY"]      = array("C001", "16",   "");

        $db = Query::dbCheckOut();

        //校種
        $sk = "";
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $query = knjc035fQuery::getNameMstA023($this);
            $getSK = $db->getOne($query);
            $sk = ($this->field["SCHOOL_KIND"]) ? $this->field["SCHOOL_KIND"] : $getSK;
        } elseif ($this->Properties["useSchool_KindField"] == "1") {
            $sk = SCHOOLKIND;
        }

        //権限グループコード
        $this->groupcd = $db->getOne(knjc035fQuery::getMinGroupcd($this, $sk));

        if ($this->field["COURSE_MAJOR"] == '' && $this->groupcd != '') {
            //MIN権限グループの課程学科初期値取得
            $query = knjc035fQuery::getMinGroupcdCourseMajor($this, $sk);
            $this->field["COURSE_MAJOR"] = $db->getOne($query);
        }

        //課程学科
        $query = knjc035fQuery::getCourseMajor($this, $sk);
        $getCM = $db->getOne($query);
        $cm = ($this->field["COURSE_MAJOR"]) ? $this->field["COURSE_MAJOR"] : $getCM;

        //名称マスタから項目名取得
        $nameMst = array();
        $query = knjc035fQuery::getAttendNameList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($this->attendItem as $attend_item => $val) {
                if ($val[0] == $row["NAMECD1"] && $val[1] == $row["NAMECD2"]) {
                    $nameMst[$attend_item] = $row["NAME1"];
                }
            }
        }

        //ADMIN_CONTROL_ATTEND_ITEMNAME_DATから項目名取得
        $itemName = array();
        $query = knjc035fQuery::getAdminControlAttendItemnameDat($this, $sk, $cm);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $itemName[$row["ATTEND_ITEM"]] = $row["ATTEND_ITEMNAME"];
        }

        //出欠管理者コントロール取得
        $this->item_array = $this->itemR_array = array();
        $this->last_field = "";
        $query = knjc035fQuery::getAdminControlAttendDat($this, $sk, $cm);

        $lastfield = '';
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //項目名
            $label = $this->attendItem[$row["ATTEND_ITEM"]][2];
            if (strlen($nameMst[$row["ATTEND_ITEM"]])) {
                $label = $nameMst[$row["ATTEND_ITEM"]];
            }
            if (strlen($itemName[$row["ATTEND_ITEM"]])) {
                $label = $itemName[$row["ATTEND_ITEM"]];
            }

            if ($row["CONTROL_DIV"] == "1") {
                $this->item_array[$row["SHOWORDER"]] = array("label"    => $label,
                                                             "item"     => $row["ATTEND_ITEM"],
                                                             "input"    => $row["INPUT_FLG"]);
            } else {
                $this->itemR_array[$row["SHOWORDER"]] = array("label"   => $label,
                                                              "item"    => $row["ATTEND_ITEM"],
                                                              "input"   => $row["INPUT_FLG"]);
                $lastfield = 'RUISEKI_' . $row["ATTEND_ITEM"];
            }
            $this->last_field = $row["ATTEND_ITEM"];
        }
        if ($lastfield != '') {
            $this->last_field = $lastfield;
        }

        $this->kessekiItem = array();
        if ($this->Properties["useAttendLessonOverHyouji"] == "1") {
            //履修上限値(学年-コースをキーとした連想配列で保持)
            $query = knjc035fQuery::getAttendLessonInfo($this);
            $result = $db->query($query);
            $this->risyuuJyougenArray = array();
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $this->risyuuJyougenArray[$row["GRADE"]."-".$row["COURSECD"]."-".$row["MAJORCD"]."-".$row["COURSECODE"]] = $row["RISYUU_JYOUGEN"];
            }
            $this->schoolMst = array();
            $query = knjc035fQuery::getSchoolMst($this);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                foreach ($row as $key => $val) {
                    $this->schoolMst[$key] = $val;
                }
            }
            $this->kessekiItem = array("KESSEKI", "SICK", "NOTICE", "NONOTICE");
            if ($this->schoolMst["SUB_OFFDAYS"] == "1") {
                $this->kessekiItem[] = "OFFDAYS";
            }
            if ($this->schoolMst["SUB_ABSENT"] == "1") {
                $this->kessekiItem[] = "ABSENT";
            }
            if ($this->schoolMst["SUB_SUSPEND"] == "1") {
                $this->kessekiItem[] = "SUSPEND";
            }
            if ($this->schoolMst["SUB_MOURNING"] == "1") {
                $this->kessekiItem[] = "MOURNING";
            }
            if ($this->schoolMst["SUB_VIRUS"] == "1") {
                $this->kessekiItem[] = "VIRUS";
            }
        }

        Query::dbCheckIn($db);
    }

    //入力項目チェック
    public function validateInput()
    {
        $db = Query::dbCheckOut();
        //更新対象データがない場合
        if ($this->field["SCHREGNO"] == null || $this->field["SCHREGNO"] == "") {
            $this->setWarning("MSG303");
            return false;
        }

        foreach ($this->field["SCHREGNO"] as $key => $schregno) {
            if ($this->dataCheck($key) ||
                (!$this->dataCheck($key) && "" != $this->field["APPOINTED_DAY"])) {
                $errMsg = $this->appDateCheck($db);
                if ("" != $errMsg) {
                    $this->setWarning("MSG901", $errMsg);
                    return false;
                }
            }
        }

        Query::dbCheckIn($db);
        return true;
    }

    //入力データ判定
    public function dataCheck($key)
    {
        if ($this->field["LESSON"][$key] != "" ||
            $this->field["OFFDAYS"][$key] != "" ||
            $this->field["ABROAD"][$key] != "" ||
            $this->field["ABSENT"][$key] != "" ||
            $this->field["SUSPEND"][$key] != "" ||
            $this->field["KOUDOME"][$key] != "" ||
            $this->field["VIRUS"][$key] != "" ||
            $this->field["MOURNING"][$key] != "" ||
            $this->field["SICK"][$key] != "" ||
            $this->field["NOTICE"][$key] != "" ||
            $this->field["NONOTICE"][$key] != "" ||
            $this->field["NURSEOFF"][$key] != "" ||
            $this->field["LATE"][$key] != "" ||
            $this->field["EARLY"][$key] != "") {
            return true;
        }
        return false;
    }

    //締め日チェック
    public function appDateCheck($db)
    {
        //締め日の最終日
        $year = CTRL_YEAR;
        if ($this->field["MONTH"] < "04") {
            $year = CTRL_YEAR + 1;
        }
        $lastday = date("t", mktime(0, 0, 0, $this->field["MONTH"], 1, $year));
        //締め日がその月の最終日より大きい場合
        if ($lastday < $this->field["APPOINTED_DAY"]) {
            return $this->field["MONTH"]."月の最終日は、".$lastday."日です。";
        }
        //締め日がNullの場合
        if ("" == $this->field["APPOINTED_DAY"]) {
            return "締め日が入力されていません。";
        }
        //締め日が0の場合
        if ("" != $this->field["APPOINTED_DAY"] && 0 == $this->field["APPOINTED_DAY"]) {
            return "締め日が0日は存在しません。";
        }
        $semeday = $db->getRow(knjc035fQuery::selectSemesAll($this->field["SEMESTER"]), DB_FETCHMODE_ASSOC);
        //学期マスタの開始日より小さい場合
        if (sprintf('%02d', $semeday["S_MONTH"]) == $this->field["MONTH"] &&
            $semeday["S_DAY"] > $this->field["APPOINTED_DAY"]) {
            return $semeday["SEMESTERNAME"]."の開始日は、".$semeday["S_MONTH"]."月".$semeday["S_DAY"]."日です。";
        }
        //学期マスタの最終日より大きい場合
        if (sprintf('%02d', $semeday["E_MONTH"]) == $this->field["MONTH"] &&
            $semeday["E_DAY"] < $this->field["APPOINTED_DAY"]) {
            return $semeday["SEMESTERNAME"]."の最終日は、".$semeday["E_MONTH"]."月".$semeday["E_DAY"]."日です。";
        }
        return;
    }

    //更新処理
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            if ($this->update()) {
                unset($this->field["LESSON_SET"]);
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    //編集対象データを更新
    public function &update()
    {
        $result = knjc035fQuery::getUpdateQuery($this);
        return $result;
    }
}

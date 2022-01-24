<?php

require_once('for_php7.php');
class knjc031aModel extends Model
{
    public $cmd;
    public $field = array();
    public $reset = array();
    public $appointed_day;
    public $lastColumn;//CSVの最後の列の値

    public function knjc031aModel()
    {
        $this->field = array();
    }

    public function init()
    {
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd               = VARS::request("cmd");
        $this->school_kind       = ($this->school_kind != "") ? $this->school_kind : "";
        $this->appointed_day     = VARS::post("SET_APPOINTED_DAY"); //締め日
        $this->field["hr_class"] = VARS::post("HR_CLASS"); //対象学級
        $this->field["month"]    = VARS::post("MONTH");    //対象月

        /* 学年・学級に分割 */
        list($this->field["grade"], $this->field["class"]) = explode("-", $this->field["hr_class"]);

        /* 編集対象データを取得 */
        $this->field["SCHREGNO"]   = VARS::post("SCHREGNO");   //学籍番号
        $this->field["LESSON"]     = VARS::post("LESSON");     //授業日数
        $this->field["OFFDAYS"]    = VARS::post("OFFDAYS");    //休学日数
        $this->field["ABROAD"]     = VARS::post("ABROAD");     //留学日数
        $this->field["ABSENT"]     = VARS::post("ABSENT");     //公欠日数
        $this->field["SUSPEND"]    = VARS::post("SUSPEND");    //出停日数
        $this->field["VIRUS"]      = VARS::post("VIRUS");      //伝染病
        $this->field["MOURNING"]   = VARS::post("MOURNING");   //忌引日数
        $this->field["NONOTICE"]   = VARS::post("NONOTICE");   //事故欠無
        $this->field["LATEDETAIL"] = VARS::post("LATEDETAIL"); //遅刻回数
        $this->field["KEKKA_JISU"] = VARS::post("KEKKA_JISU"); //欠課時数
        $this->field["KEKKA"]      = VARS::post("KEKKA");      //早退回数
        $this->reset = array();
        if (is_array($this->field["SCHREGNO"])) {
            foreach ($this->field["SCHREGNO"] as $key => $schregno) {
                $this->reset[$schregno] = array("LESSON"     => $this->field["LESSON"][$key],
                                                "OFFDAYS"    => $this->field["OFFDAYS"][$key],
                                                "ABROAD"     => $this->field["ABROAD"][$key],
                                                "ABSENT"     => $this->field["ABSENT"][$key],
                                                "SUSPEND"    => $this->field["SUSPEND"][$key],
                                                "VIRUS"      => $this->field["VIRUS"][$key],
                                                "MOURNING"   => $this->field["MOURNING"][$key],
                                                "NONOTICE"   => $this->field["NONOTICE"][$key],
                                                "LATEDETAIL" => $this->field["LATEDETAIL"][$key],
                                                "KEKKA_JISU" => $this->field["KEKKA_JISU"][$key],
                                                "KEKKA"      => $this->field["KEKKA"][$key]);
            }
        }

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    /* 入力項目チェック */
    public function validateInput()
    {
        $db = Query::dbCheckOut();

        if (!isset($this->field["SCHREGNO"])) {
            $this->setMessage("MSG304");
            return false;
        }

        $monthAr = preg_split("/-/", $this->field["month"]);

        //締め日の最終日
        $year = CTRL_YEAR;
        if ($monthAr[0] < "04") {
            $year = CTRL_YEAR + 1;
        }
        $lastday = date("t", mktime(0, 0, 0, $monthAr[0], 1, $year));
        //締め日がその月の最終日より大きい場合
        if ($lastday < $this->appointed_day) {
            $this->setWarning("MSG901", $monthAr[0]."月の最終日は、".$lastday."日です。");
            return false;
        }
        //締め日が0の場合
        if (0 == $this->appointed_day) {
            $this->setWarning("MSG901", "0日は存在しません。");
            return false;
        }

        $semeday = $db->getRow(knjc031aQuery::selectSemesAll($monthAr[1]), DB_FETCHMODE_ASSOC);
        //学期マスタの開始日より小さい場合
        if (sprintf('%02d', $semeday["S_MONTH"]) == $monthAr[0] && $semeday["S_DAY"] > $this->appointed_day) {
            $this->setWarning("MSG901", $semeday["SEMESTERNAME"]."の開始日は、".$semeday["S_MONTH"]."月".$semeday["S_DAY"]."日です。");
            return false;
        }
        //学期マスタの最終日より大きい場合
        if (sprintf('%02d', $semeday["E_MONTH"]) == $monthAr[0] && $semeday["E_DAY"] < $this->appointed_day) {
            $this->setWarning("MSG901", $semeday["SEMESTERNAME"]."の最終日は、".$semeday["E_MONTH"]."月".$semeday["E_DAY"]."日です。");
            return false;
        }

        Query::dbCheckIn($db);

        return true;
    }

    /* 更新処理 */
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            if ($this->update()) {
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    /* 編集対象データを更新 */
    public function &update()
    {
        $result = knjc031aQuery::getUpdateQuery($this);
        return $result;
    }
}

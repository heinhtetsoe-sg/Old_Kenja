<?php

require_once('for_php7.php');

class knjc031dModel extends Model {

    var $cmd;
    var $field = array();
    var $reset = array();
    var $appointed_day;

    function knjc031dModel()
    {
        $this->field = array();
    }

    function init()
    {
        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->cmd                      = VARS::request("cmd");
        $this->appointed_day            = VARS::post("SET_APPOINTED_DAY");  //締め日
        $this->field["HR_CLASS_TYPE"]   = VARS::post("HR_CLASS_TYPE");      //クラス方式
        $this->field["hr_class"]        = VARS::post("HR_CLASS");           //対象学級
        $this->field["month"]           = VARS::post("MONTH");              //対象月

        if ($this->cmd == "update") {
            $this->field["HR_CLASS_TYPE"]   = VARS::post("HIDDEN_HR_CLASS_TYPE");
            $this->field["hr_class"]        = VARS::post("HIDDEN_HR_CLASS");
            $this->field["month"]           = VARS::post("HIDDEN_MONTH");
        }

        /* 学年・学級に分割 */
        //特別支援のみ
        if ($this->field["HR_CLASS_TYPE"] == "2" && $this->Properties["useFi_Hrclass"] != "1" && $this->Properties["useSpecial_Support_Hrclass"] == "1") {
            $this->field["class"] = $this->field["hr_class"];
        } else {
            list ($this->field["grade"], $this->field["class"]) = explode("-",$this->field["hr_class"]);
        }
        /* 編集対象データを取得 */
        $this->field["SCHREGNO"]        = VARS::post("SCHREGNO");           //学籍番号
        $this->field["LESSON"]          = VARS::post("LESSON");             //授業日数
        $this->field["OFFDAYS"]         = VARS::post("OFFDAYS");            //休学日数
        $this->field["ABROAD"]          = VARS::post("ABROAD");             //留学日数
        $this->field["ABSENT"]          = VARS::post("ABSENT");             //公欠日数
        $this->field["SUSPEND"]         = VARS::post("SUSPEND");            //出停日数
        $this->field["MOURNING"]        = VARS::post("MOURNING");           //忌引日数
        $this->field["SICK"]            = VARS::post("SICK");               //欠席
        $this->field["NOTICE"]          = VARS::post("NOTICE");             //事故欠届
        $this->field["NONOTICE"]        = VARS::post("NONOTICE");           //事故欠無
        $this->field["LATE"]            = VARS::post("LATE");               //遅刻回数
        $this->field["EARLY"]           = VARS::post("EARLY");              //早退回数
        $this->field["DETAIL_101"]      = VARS::post("DETAIL_101");         //DETAIL

        $this->notUpdateItem            = VARS::post("NOT_UPDATE_ITEM");

        /* 大分類の編集対象データ取得 */
        $db = Query::dbCheckOut();
        $sub_data = $db->getCol(knjc031dQuery::getNameMst('C006', '6'));
        foreach($sub_data as $subkey) {
            $this->field["CNT".$subkey] = VARS::post("CNT".$subkey);
        }

        //C002
        $this->c002 = array();
        $query = knjc031dQuery::getDetailDiv();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->c002[$row["VALUE"]] = "DETAIL_".$row["VALUE"];
        }
        $result->free();

        //校種
        $this->schoolkind = "";
        if ($this->Properties["useSchool_KindField"] == "1" && $this->field["hr_class"]) {
            if ($this->field["HR_CLASS_TYPE"] == "2" && $this->Properties["useFi_Hrclass"] != "1") {
                $this->schoolkind = "";
            } else if ($this->Properties["use_prg_schoolkind"] != "1") {
                $this->schoolkind = SCHOOLKIND;
            } else {
                $this->schoolkind = $db->getOne(knjc031dQuery::getSchoolKind($this));
            }
        }

        Query::dbCheckIn($db);

        $this->reset = array();
        if (is_array($this->field["SCHREGNO"])) {
            foreach ($this->field["SCHREGNO"] as $key => $schregno) {
                $this->reset[$schregno] = array("LESSON"       => $this->field["LESSON"][$key],
                                                "OFFDAYS"      => $this->field["OFFDAYS"][$key],
                                                "ABROAD"       => $this->field["ABROAD"][$key],
                                                "ABSENT"       => $this->field["ABSENT"][$key],
                                                "SUSPEND"      => $this->field["SUSPEND"][$key],
                                                "MOURNING"     => $this->field["MOURNING"][$key],
                                                "SICK"         => $this->field["SICK"][$key],
                                                "NOTICE"       => $this->field["NOTICE"][$key],
                                                "NONOTICE"     => $this->field["NONOTICE"][$key],
                                                "LATE"         => $this->field["LATE"][$key],
                                                "EARLY"        => $this->field["EARLY"][$key],
                                                "DETAIL_101"   => $this->field["DETAIL_101"][$key]);

                foreach($sub_data as $subkey) {
                        $this->reset[$schregno]["CNT".$subkey] = $this->field["CNT".$subkey][$key];
                }
            }
        }
    }

    /* 入力項目チェック */
    function validateInput()
    {
        $db = Query::dbCheckOut();

        if(!isset($this->field["SCHREGNO"])){
            $this->setMessage("MSG304");
            return false;
        }

        $monthAr = preg_split("/-/", $this->field["month"]);

        //締め日の最終日
        $year = CTRL_YEAR;
        if ($monthAr[0] < "04") {
            $year = CTRL_YEAR + 1;
        }
        $lastday = date("t", mktime( 0, 0, 0, $monthAr[0], 1, $year ));
        //締め日がその月の最終日より大きい場合
        if ($lastday < $this->appointed_day) {
            $this->setWarning("MSG901",$monthAr[0]."月の最終日は、".$lastday."日です。");
            return false;
        }
        //締め日が0の場合
        if (0 == $this->appointed_day) {
            $this->setWarning("MSG901","0日は存在しません。");
            return false;
        }

        $semeday = $db->getRow(knjc031dQuery::selectSemesAll($monthAr[1]),DB_FETCHMODE_ASSOC);
        //学期マスタの開始日より小さい場合
        if (sprintf('%02d', $semeday["S_MONTH"]) == $monthAr[0] &&
            $semeday["S_DAY"] > $this->appointed_day) {
            $this->setWarning("MSG901",$semeday["SEMESTERNAME"]."の開始日は、".$semeday["S_MONTH"]."月".$semeday["S_DAY"]."日です。");
            return false;
        }
        //学期マスタの最終日より大きい場合
        if (sprintf('%02d', $semeday["E_MONTH"]) == $monthAr[0] &&
            $semeday["E_DAY"] < $this->appointed_day) {
            $this->setWarning("MSG901",$semeday["SEMESTERNAME"]."の最終日は、".$semeday["E_MONTH"]."月".$semeday["E_DAY"]."日です。");
            return false;
        }

        Query::dbCheckIn($db);

        return true;
    }

    /* 更新処理 */
    function &getUpdateModel()
    {
        if ($this->validateInput()){
            if($this->update()){
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    /* 編集対象データを更新 */
    function &update()
    {
        $result = knjc031dQuery::getUpdateQuery($this);
        return $result;
    }
}
?>

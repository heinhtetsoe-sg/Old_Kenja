<?php

require_once('for_php7.php');

class knjz352cModel extends Model {
    var $field = array();
    var $data  = array();
    var $cmd;

    function knjz352cModel() {}

    function init() {

        if (VARS::request("PROGRAMID")) {
            $this->field = array();
            common::GetControlMaster_Fnc($this->control);
        }

        $this->field["ATTEND_DIV"]      = VARS::post("ATTEND_DIV");
        $this->field["GRADE"]           = "00";
        if ($this->Properties["use_school_detail_gcm_dat"] == "1") {
            $this->field["COURSE_MAJOR"]    = VARS::post("COURSE_MAJOR");
        } else {
            $this->field["COURSE_MAJOR"]    = "0-000";
        }
        $this->cmd = VARS::request("cmd");

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //学校種別
        if (VARS::post("SCHOOL_KIND")) {
            $this->school_kind = VARS::post("SCHOOL_KIND");
        }

        //出欠項目一覧
        $this->attendItem = array();
        $this->attendItem[]["LESSON"]       = array("0000", "",     array("1", "授業日数"), array("1", "授業時数"));
        $this->attendItem[]["OFFDAYS"]      = array("A004", "2",    array("1", ""), array("1", ""));
        $this->attendItem[]["ABROAD"]       = array("A004", "1",    array("1", ""), array("1", ""));
        $this->attendItem[]["ABSENT"]       = array("C001", "1",    array("1", ""), array("1", ""));
        $this->attendItem[]["SUSPEND"]      = array("C001", "2",    array("1", ""), array("1", ""));
        if ($this->Properties["useKoudome"] == "true") {
            $this->attendItem[]["KOUDOME"]      = array("C001", "25",   array("1", ""), array("1", ""));
        }
        if ($this->Properties["useVirus"] == "true") {
            $this->attendItem[]["VIRUS"]        = array("C001", "19",   array("1", ""), array("1", ""));
        }
        $this->attendItem[]["MOURNING"]     = array("C001", "3",    array("1", ""), array("1", ""));
        $this->attendItem[]["ATTEND"]       = array("0000", "",     array("1", "出席すべき日数"), array("", ""));
        $this->attendItem[]["SICK"]         = array("C001", "4",    array("1", ""), array("1", ""));
        $this->attendItem[]["NOTICE"]       = array("C001", "5",    array("1", ""), array("1", ""));
        $this->attendItem[]["NONOTICE"]     = array("C001", "6",    array("1", ""), array("1", ""));
        $this->attendItem[]["KESSEKI"]      = array("0000", "",     array("1", "欠席日数"), array("", ""));
        $this->attendItem[]["PRESENT"]      = array("0000", "",     array("1", "出席日数"), array("", ""));
        $this->attendItem[]["NURSEOFF"]     = array("C001", "14",   array("",  ""), array("1", ""));
        $this->attendItem[]["LATE"]         = array("C001", "15",   array("1", ""), array("1", ""));
        $this->attendItem[]["EARLY"]        = array("C001", "16",   array("1", ""), array("1", ""));
        $this->attendItem[]["DETAIL_001"]   = array("C002", "001",  array("1", ""), array("",  ""));
        $this->attendItem[]["DETAIL_002"]   = array("C002", "002",  array("1", ""), array("",  ""));
        $this->attendItem[]["DETAIL_003"]   = array("C002", "003",  array("1", ""), array("",  ""));
        $this->attendItem[]["DETAIL_004"]   = array("C002", "004",  array("1", ""), array("",  ""));
        $this->attendItem[]["DETAIL_101"]   = array("C002", "101",  array("1", ""), array("",  ""));
        $this->attendItem[]["DETAIL_102"]   = array("C002", "102",  array("1", ""), array("",  ""));

        //名称マスタから項目名取得
        $db = Query::dbCheckOut();
        $nameMst = array();
        $query = knjz352cQuery::getAttendNameList($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $nameMst[$row["NAMECD1"]."_".$row["NAMECD2"]] = $row["NAME1"];
        }
        Query::dbCheckIn($db);

        //出欠項目一覧取得
        $this->item_array = array();
        foreach ($this->attendItem as $key => $array) {
            foreach ($array as $field => $val) {
                if ($val[0] != "0000" && !strlen($nameMst[$val[0]."_".$val[1]])) continue;
                for ($i = 1; $i <= 2; $i++) {
                    if ($val[$i+1][0] == "1") {
                        $label = $val[$i+1][1];
                        if ($val[0] != "0000") {
                            $label = $nameMst[$val[0]."_".$val[1]];
                        }
                        $this->item_array[$i][] = array("key"     => $key,
                                                        "label"   => $label,
                                                        "value"   => $field);
                    }
                }
            }
        }

        $this->fields = array();
        for ($i = 1; $i <= 2; $i++) {
            foreach ($this->item_array[$i] as $key => $val) {
                $attend_itemname = "ATTEND_ITEMNAME-".$val["value"];
                $this->fields["ATTEND_ITEMNAME"][$val["value"]] = VARS::post($attend_itemname);
            }
        }
    }

    //入力項目チェック
    function validateInput() {
        if ($this->Properties["use_school_detail_gcm_dat"] == '1' && !strlen($this->field["COURSE_MAJOR"])) {
            $this->warning = "課程学科を選択してください。";
            return false;
        }

        foreach ($this->item_array[$this->field["ATTEND_DIV"]] as $key => $val) {
            if ($this->validate_row_cnt($this->fields["ATTEND_ITEMNAME"][$val["value"]], (10 * 2)) > 1) {
                $this->setWarning("MSG915","( {$val["label"]}は全角10文字までです )");
                return false;
            }
        }
        return true;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            knjz352cQuery::updateQuery($this);
            $this->setMessage("MSG201");
        }
    }
    /**
     * 前年度からのコピーのモデルを返します
    */
    function &getCopyModel(){
        $db = Query::dbCheckOut();
        $pre_year = CTRL_YEAR - 1;
        $preYear_cnt = $db->getOne(knjz352cQuery::getCopyData($pre_year, "cnt"));
        //前年度データチェック
        if ($preYear_cnt == 0) {
            $this->setWarning("MSG203", "前年度データがありません。");
            return false;
        }
        Query::dbCheckIn($db);
        knjz352cQuery::getCopyQuery($pre_year);
        $this->setMessage("MSG201");
    }
}
?>

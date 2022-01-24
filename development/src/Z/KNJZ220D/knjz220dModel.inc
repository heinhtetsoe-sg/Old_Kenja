<?php

require_once('for_php7.php');

class knjz220dModel extends Model {

    var $field1      = array();
    var $field2      = array();
    var $cmd;
    var $grade;
    var $classcd;
    var $copy        = array();
    var $Clean;
    var $TBL_COUNT;                 //テーブルの最大レコード数
    var $default_val = array();     //デフォルト値
    var $assesslevelcnt;
    //Constructor
    function knjz220dModel() {}

    function init()
    {
        $this->cmd          = VARS::request("cmd");

        if (VARS::get("PROGRAMID") || VARS::get("inir")) {
            $this->semester = "";
            $this->grade = "";
            $this->classcd = "";
            $this->copy = array();
            $this->assesslevelcnt = "";
        }
        if ($this->cmd == 'copy') {
            $this->assesslevelcnt = "";
        } else {
            if (VARS::post("ASSESSLEVELCNT")) $this->assesslevelcnt = VARS::post("ASSESSLEVELCNT");
        }
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        // 学期
        if (VARS::post("SEMESTER")) {
            $this->semester     = VARS::post("SEMESTER");
        } else if (VARS::get("SEMESTER")) {
            $this->semester     = VARS::get("SEMESTER");
        }
        if ($this->semester == '') {
            $this->semester = '9';
        }
        $db = Query::dbCheckOut();
        $query = knjz220dQuery::getSemestername($this);
        $this->semestername = $db->getOne($query);

        //学年
        $result = $db->query(knjz220dQuery::combo_grdQuery($this));
        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        if (!$this->grade) $this->grade = $row["VALUE"];

        //学年名称取得
        $this->grade_name = array();
        $query = knjz220dQuery::combo_grdQuery($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->grade_name[$row["VALUE"]] = $row["LABEL"];
        }

        $this->grade   = VARS::request("GRADE") ? VARS::request("GRADE") : $this->grade;
        $this->classcd = VARS::request("CLASSCD") ? VARS::request("CLASSCD") : $this->classcd;

        //デフォルト値取得
        $this->default_val = array("LOW"  => VARS::post("default_val_low"),
                                   "HIGH" => VARS::post("default_val_high"));
        //フィールドのレコード数取得
        $this->TBL_COUNT = VARS::post("TBL_COUNT");

        //最大値、最小値を取得
        if ($this->default_val["LOW"] != "" && $this->default_val["HIGH"] != "") {
            $min = explode(",", $this->default_val["LOW"]);
            $max = explode(",", $this->default_val["HIGH"]);
            $this->default_val["LOW"]  = $min[0];
            $this->default_val["HIGH"] = $max[($this->TBL_COUNT-1)];
        }

        $this->field1 = array( "SUBCLASSCD"     =>  VARS::request("SUBCLASSCD"),   //科目コード
                               "UPDATED"        =>  VARS::post("UPDATED"));        //更新日付


        $this->field2 = array( "SUBCLASSCD"     =>  VARS::request("SUBCLASSCD2"),   //科目コード
                               "UPDATED"        =>  VARS::post("UPDATED"));        //更新日付

        if ($this->assesslevelcnt == '') {
            if (!isset($this->warning)) {
                if ($this->cmd == 'copy') {
                    $this->copy["FLG"] = true;
                    $this->copy["SELECT"] = VARS::post("COPY_SELECT");
                }
                $query = knjz220dQuery::selectQuery($this);
                $c = get_count(knjz220dModel::fetchRows($query, $db));
                if ($c) {
                    $this->assesslevelcnt = $c;
                }
            }
        }
        if ($this->assesslevelcnt == '' || $this->cmd == 'setdef') {
            $this->assesslevelcnt = $this->TBL_COUNT;
        }
        Query::dbCheckIn($db);

        $this->assesslevelcnt = '5';

        //テーブルのレコードの数だけ取得
        for ($i = 2; $i <= $this->assesslevelcnt; $i++) {
            $this->field2["ASSESSLOW".($i)]  = VARS::post("ASSESSLOW".($i));
            $this->field2["ASSESSMARK".($i)] = VARS::post("ASSESSMARK".($i));            
            $this->field2["PERCENT".($i)] = VARS::post("PERCENT".($i));
        }

        $this->Clean        = VARS::post("Cleaning");
        $this->copy["FLG"]  = false;
        $this->copy["SELECT"] = VARS::post("COPY_SELECT");

    }

    function fetchRows($query, $db0="") {
        $rows = array();

        if ($db0 != "") {
            $result = $db0->query($query);
        } else {
            $db = Query::dbCheckOut();
            $result = $db->query($query);
        }
        while ($row  = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rows[] = $row;
        }
        if ($db0 != "") {
        } else {
            Query::dbCheckIn($db);
        }
        return $rows;
    }

    function createOpts($query, $db0="") {
        return array_map(function($row) { return array("value" => htmlspecialchars($row["VALUE"]), "label" => $row["LABEL"]);}, knjz220dModel::fetchRows($query, $db0));
    }

    function validateInput() {
        $chk[] = array();

        //field値チェック
        if ($this->field1["SUBCLASSCD"] == "") {
            $this->setWarning("MSG303");
            return false;
        } else if ($this->grade == "") {
            $this->setWarning("MSG303");
            return false;
        }

        //UPDATEチェック
        if (!is_null($this->field1["UPDATED"]) && $this->field1["UPDATED"] != "") {
            $up = implode(",", array_map(function($row) { return $row["UPDATED"]; }, knjz220dModel::fetchRows(knjz220dQuery::selectQuery($this))));
            if ($this->field1["UPDATED"] != $up) {
                $this->setWarning("MSG307");
                return false;
            }
        }

        //最大値チェック
        if (!isset($this->assesslevelcnt)) {
            $this->setWarning("MSG917");
            return false;
        }

        //テキストエリア入力チェック
        for ($i = 2; $i <= $this->assesslevelcnt; $i++) {

            $val = $this->field2["PERCENT".($i)];
            $sum_percent = $sum_percent + $this->field2["PERCENT".($i)];

            // 空か数値以外か
            if (!isset($val) || $val == "" || !is_numeric($val)) {
                $this->setWarning("MSG301");
                return false;
            }

            // 最大値 or 最小値 かどうか？
            /*
            if ($val < $this->default_val["LOW"] || $val > $this->default_val["HIGH"]) {
                $this->setWarning("MSG901", $val);
                return false;
            }
            $chk[] = $val;
            */
        }
        // PERCENTの合計値が100か
        if($sum_percent != 100){
            $this->setWarning("基準値の合計が100ではありません。");
            return false;
        }

        //１レコード取得
        $row = "";
        $row = knjz220dQuery::getRowcnt($this);

        if ($this->cmd == "add") {
            //追加時に既に登録されていたらエラー
            if ($row >= '1') {
                $this->setWarning("更新するのであれば、更新ボタンを押してください。");
                return false;
            }
        }
        if ($this->cmd == "update") {
            //更新時に既に登録されていなかったらエラー
            if ($row == 0) {
                $this->setWarning("新規追加であれば、追加ボタンを押してください。");
                return false;
            }
        }


        //値大小チェック
        /*
        for ($i = 1; $i < ($this->assesslevelcnt); $i++) {
            if ($i == 1) {
                if ($chk[$i] < $this->default_val["LOW"]) {
                    $this->setWarning("MSG901", $chk[$i]);
                    return false;
                }
            } else if($i == ($this->assesslevelcnt - 1)) {
                if ($chk[$i] > $this->default_val["HIGH"]) {
                    $this->setWarning("MSG901", $chk[$i]);
                    return false;
                }
            } else {
                if ($chk[$i] > $chk[($i + 1)]) {
                    $this->setWarning("MSG901", $chk[$i]);
                    return false;
                }
            }
        }*/

        return true;
    }
    //追加処理
    function &getInsertModel() {
        if ($this->validateInput()) {
            if ($this->insert()) {
                $this->setMessage("保存しました。");
            }
        }
    }

    function &insert() {
        //評定マスタデータ削除
        $result = knjz220dQuery::getInsertQuery($this);
        return $result;
    }

    //更新処理
    function &getUpdateModel() {
        if ($this->validateInput()) {
            if ($this->update()) {
                $this->setMessage("MSG201");
            }
        }
    }

    function &update() {
        //評定マスタデータ削除
        $result = knjz220dQuery::getDeleteQuery($this);
        //評定マスタデータ追加
        $result = knjz220dQuery::getInsertQuery($this);

        return $result;
    }

    function copy_data() {
        $this->copy["FLG"] = true;
        return true;
    }
}
?>

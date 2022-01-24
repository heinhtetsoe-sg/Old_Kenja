<?php

require_once('for_php7.php');

class knjf163Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $selectdata;

    function knjf163Model() {
        common::GetControlMaster_Fnc($this->control);
    }
    function init() {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->field = array("YEAR"   =>  VARS::post("YEAR"),   //年度
                             "GRADE"  =>  VARS::post("GRADE")   //学年
                            );

        $this->cmd = VARS::request("cmd");

        $this->selectdata = VARS::post("selectdata");
    }

    function getDownloadModel() {
        //変数定義
        $comma = ","; //カンマ
        $lf = "\n";   //改行
        //DB接続
        $db = Query::dbCheckOut();

        //生徒項目名切替処理
        $sch_label = "";
        //テーブルの有無チェック
        $query = knjf163Query::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && ($this->field["GRADE"] || $this->Properties["use_prg_schoolkind"] == "1")) {
            //生徒項目名取得
            $sch_label = $db->getOne(knjf163Query::getSchName($this));
        } elseif ($table_cnt > 0 && ($this->field["GRADE"] || ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""))) {
            //生徒項目名取得
            $sch_label = $db->getOne(knjf163Query::getSchName($this));
        }
        $sch_label = (strlen($sch_label) > 0) ? $sch_label : '生徒';

        //ヘッダ配列
        $header = array("学籍番号",
                        "学年",
                        "組",
                        "出席番号",
                        $sch_label."氏名",

                        "加入者名",
                        "加入者記号",
                        "加入者番号",
                        "発行機関",
                        "保険者番号",
                        "有効期限",
                        $sch_label."の認定年月",
                        "加入者との続柄",

                        "父の名前",
                        "父のふりがな",
                        "母の名前",
                        "母のふりがな",

                        "氏名1",
                        "続柄1",
                        "連絡先名称1",
                        "電話1-1",
                        "電話1-2",
                        "氏名2",
                        "続柄2",
                        "連絡先名称2",
                        "電話2-1",
                        "電話2-2",

                        "氏名3",
                        "続柄3",
                        "連絡先名称3",
                        "電話3-1",
                        "電話3-2",
                        "氏名4",
                        "続柄4",
                        "連絡先名称4",
                        "電話4-1",
                        "電話4-2",

                        "山上、海浜学校への注意"
                        );
        //ヘッダ配列を出力変数にセット
        $headerContents = implode($header, $comma) .$lf;

        //名称マスタを取得し配列にセット
        $nameF240 = array();
        $query = knjf163Query::getNameMst("F240");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $nameF240[$row["NAMECD2"]] = $row["NAME1"];
        }
        $result->free();

        //CSVデータを取得し出力変数にセット
        $data = false;
        $dataContents = "";
        $grade_hr_class_array = explode(',', $this->selectdata);
        foreach ($grade_hr_class_array as $grade_hr_class) {
            $query = knjf163Query::getCsvQuery($grade_hr_class);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $dataContents .= $row["SCHREGNO"]           . ",";
                $dataContents .= $row["GRADE"]              . ",";
                $dataContents .= $row["HR_CLASS"]           . ",";
                $dataContents .= $row["ATTENDNO"]           . ",";
                $dataContents .= $row["NAME_SHOW"]          . ",";

                $dataContents .= $row["INSURED_NAME"]           . ",";
                $dataContents .= $row["INSURED_MARK"]           . ",";
                $dataContents .= $row["INSURED_NO"]             . ",";
                $dataContents .= $row["INSURANCE_NAME"]         . ",";
                $dataContents .= $row["INSURANCE_NO"]           . ",";
                $dataContents .= $row["VALID_DATE"]             . ",";
                $dataContents .= $row["AUTHORIZE_DATE"]         . ",";
                $dataContents .= $nameF240[$row["RELATIONSHIP"]]. ",";

                $dataContents .= $row["FATHER_RELANAME"]    . ",";
                $dataContents .= $row["FATHER_RELAKANA"]    . ",";
                $dataContents .= $row["MOTHER_RELANAME"]    . ",";
                $dataContents .= $row["MOTHER_RELAKANA"]    . ",";

                $dataContents .= $row["EMERGENCYNAME"]      . ",";
                $dataContents .= $row["EMERGENCYRELA_NAME"] . ",";
                $dataContents .= $row["EMERGENCYCALL"]      . ",";
                $dataContents .= $row["EMERGENCYTELNO"]     . ",";
                $dataContents .= ""                         . ",";
                $dataContents .= $row["EMERGENCYNAME2"]     . ",";
                $dataContents .= $row["EMERGENCYRELA_NAME2"]. ",";
                $dataContents .= $row["EMERGENCYCALL2"]     . ",";
                $dataContents .= $row["EMERGENCYTELNO2"]    . ",";
                $dataContents .= ""                         . ",";

                $dataContents .= ""                         . ",";
                $dataContents .= ""                         . ",";
                $dataContents .= ""                         . ",";
                $dataContents .= ""                         . ",";
                $dataContents .= ""                         . ",";
                $dataContents .= ""                         . ",";
                $dataContents .= ""                         . ",";
                $dataContents .= ""                         . ",";
                $dataContents .= ""                         . ",";
                $dataContents .= ""                         . ",";

                $dataContents .= $row["ATTENTION"];
                $dataContents .= $lf;

                $data = true;
            }
        }

        //DB切断
        Query::dbCheckIn($db);
        //CSV出力
        $contents  = "";
        $contents .= $headerContents;
        $contents .= $dataContents;
        if ($data) {
            $filename = sprintf("%s.csv", CTRL_YEAR . "年度_保健室記入情報");
            common::downloadContents($contents, $filename);
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>

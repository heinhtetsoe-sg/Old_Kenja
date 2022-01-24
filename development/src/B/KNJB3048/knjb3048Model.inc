<?php

require_once('for_php7.php');

class knjb3048Model extends Model {
    var $field = array();
    var $cmd;

    function knjb3048Model() {}

    function init() {

        $this->cmd = VARS::request("cmd");

        $this->field = array("YEAR"         =>  VARS::post("YEAR"),
                             "SEMESTER"     =>  VARS::post("SEMESTER"),
                             "OUTPUT"       =>  VARS::post("OUTPUT"),
                             "HEADER"       =>  VARS::post("HEADER")
                            );

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //CSV出力
    function getDownloadModel() {
        //DB接続
        $db = Query::dbCheckOut();

        //初期化
        $contents = "";
        $array = array();

        //クラスシート
        $array[1]["BLANK"]              = "科・コメント";
        $array[1]["HR_NAMEABBV"]        = "年組略称";
        $array[1]["HR_CLASS_NAME1"]     = "組略称1";
        $array[1]["HR_CLASS_NAME2"]     = "組略称2";
        $array[1]["GRADE_NAME1"]        = "学年";

        //特別教室
        $array[2]["BLANK"]              = "科・コメント";
        $array[2]["FACILITYNAME"]       = "施設名";
        $array[2]["FACILITYABBV"]       = "施設略称";

        //科目
        $array[3]["CLASSNAME"]          = "教科";
        $array[3]["SUBCLASSNAME"]       = "科目名称";
        $array[3]["SUBCLASSCD"]         = "科目コード";
        $array[3]["SUBCLASSABBV"]       = "科目略称";

        //先生
        $array[4]["STAFFNAME"]          = "職員氏名";
        $array[4]["STAFFCD"]            = "職員コード";
        $array[4]["STAFFNAME_SHOW"]     = "職員氏名表示用";
        $array[4]["SECTIONNAME"]        = "所属";
        $array[4]["BLANK"]              = "学年";
        $array[4]["HR_CLASS_NAME1"]     = "主担任クラス";
        $array[4]["PART_TIME"]          = "常勤・非常勤・休憩不要";

        //ヘッダ出力
        if ($this->field["HEADER"]) {
            $sep = "";
            foreach ($array[$this->field["OUTPUT"]] as $field => $label) {
                $contents .= $sep.$label;
                $sep = ",";
            }
            $contents .= "\n";
        }

        if ($this->field["OUTPUT"] == "1") {
            $query = knjb3048Query::getCsvQuery1($this);
            $subTitle = "クラスシート";
        } else if ($this->field["OUTPUT"] == "2") {
            $query = knjb3048Query::getCsvQuery2($this);
            $subTitle = "特別教室";
        } else if ($this->field["OUTPUT"] == "3") {
            $query = knjb3048Query::getCsvQuery3($this);
            $subTitle = "科目";
        } else if ($this->field["OUTPUT"] == "4") {
            $query = knjb3048Query::getCsvQuery4($this);
            $subTitle = "先生";
        }

        //データ出力
        $data = false;
        if ($query) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $sep = "";
                foreach ($array[$this->field["OUTPUT"]] as $field => $label) {

                    if ($field == "SUBCLASSCD") $row[$field] = substr($row[$field], 0, 4);

                    $contents .= $sep.$row[$field];
                    $sep = ",";
                }
                $contents .= "\n";
                $data = true;
            }
            $result->free();
        }

        //学期名取得
        $seme = "";
        if ($this->field["OUTPUT"] == "1") {
            $seme = $db->getOne(knjb3048Query::getSemester($this, $this->field["SEMESTER"]));
        }

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
            $title = $this->field["YEAR"]."年度".$seme." AI時間割設定（".$subTitle."）";
            $filename = sprintf("%s.csv", $title);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
}
?>

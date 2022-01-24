<?php

require_once('for_php7.php');
class knjz235wModel extends Model
{
    public $field = array();
    public $field2 = array();
    public $cmd;
    public $pattern;
    public $frmPatern;

    public function knjz235wModel()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID") != "") {
            unset($this->field);
            unset($this->schoolKind);
        }
        
        $this->getPropertiesAll(); //プロパティファイルの読込み

        $db = Query::dbCheckOut();
        $proCnt  = 0;
        $proKind = "";
        $this->SchKindOpt = $schKindArr = array();
        $query = knjz235wQuery::getSchoolKind($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schKindArr[$row["VALUE"]] = $row["VALUE"];

            if ($this->Properties["knjz235wPattern_".$row["VALUE"]]) {
                $this->SchKindOpt[] = array("label" => $row["LABEL"],
                                            "value" => $row["VALUE"]
                                            );

                $proCnt++;
                $proKind = $row["VALUE"];
            }
        }

        // 学期情報取得
        $this->semesterList = array();
        $query = knjz235wQuery::getSemesterAll();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $this->semesterList[] = $row;
        }
        $result->free();

        // 最終学期情報取得
        $this->semesterMax = $db->getOne(knjz235wQuery::getSemesterMax());
        Query::dbCheckIn($db);

        if ($proCnt > 0) {
            $this->schoolKind = VARS::post("SCHOOL_KIND");
            $this->schoolKind = ($this->schoolKind != "") ? $this->schoolKind: $this->SchKindOpt[0]["value"];
        }

        if ($proCnt == 0) {
            $Pattern = $this->Properties["knjz235wPattern"];
            if (strpos($Pattern, 'KNJ') === 0) {
                $this->pattern = substr($Pattern, 3);
            } else {
                $this->pattern = 'D186W';
            }
        } elseif ($proCnt > 1) {
            if (strpos($this->Properties["knjz235wPattern_".$this->schoolKind], 'KNJ') === 0) {
                $this->pattern = substr($this->Properties["knjz235wPattern_".$this->schoolKind], 3);
            } else {
                $this->pattern = 'D186W';
            }
        } elseif ($proCnt == 1) {
            if (strpos($this->Properties["knjz235wPattern_".$proKind], 'KNJ') === 0) {
                $this->pattern = substr($this->Properties["knjz235wPattern_".$proKind], 3);
            } else {
                $this->pattern = 'D186W';
            }
        }

        //校種(佐賀は校種コンボ表示する)
        if ($this->Properties["knjz235wPattern"] == "KNJD429") {
            $this->pattern    = 'D429';
        } elseif ($this->Properties["knjz235wPattern"] == "KNJD429BD") {
            $this->pattern    = 'D429BD';
        } elseif ($this->Properties["knjz235wPattern"] == "KNJD429LM") {
            $this->pattern    = 'D429LM'; //宮城
        }

        if ($this->pattern == 'D429LM') {
            //入力文字数の設定（教育目標・目指す生徒像）
            $this->seq013_moji = 60;
            $this->seq013_gyou = 7;
        }

        if ($this->pattern == 'D429LM') {
            $this->field = array("SEQ001"       =>  VARS::post("SEQ001"),       //帳票表題
                                 "SEQ002"       =>  VARS::post("SEQ002"),       //主に使用する様式
                                 "SEQ003"       =>  VARS::post("SEQ003"),       //担任表示
                                 "SEQ004"       =>  VARS::post("SEQ004"),       //クラス表示
                                 "SEQ008"       =>  VARS::post("SEQ008"),       //行動の記録
                                 "SEQ009"       =>  VARS::post("SEQ009"),       //年間目標
                                 "SEQ012"       =>  VARS::post("SEQ012"),       //家庭より
                                 "SEQ013"       =>  VARS::post("SEQ013"),       //教育目標・目指す生徒像
                                 "DBNAME"       =>  VARS::post("DBNAME")
                                );
            //帳票パターン
            $this->field2 = array("TYOUHYOU_PATTERN"      =>  VARS::post("TYOUHYOU_PATTERN"));
        } elseif ($this->pattern == 'D429BD') {
            $this->field = array("SEQ001"       =>  VARS::post("SEQ001"),       //帳票表題
                                 "SEQ002"       =>  VARS::post("SEQ002"),       //主に使用する様式
                                 "SEQ004"       =>  VARS::post("SEQ004"),       //クラス表示
                                 "SEQ011"       =>  VARS::post("SEQ011"),       //学校より
                                 "SEQ010_1"     =>  VARS::post("SEQ010_1"),     //総合所見
                                 "SEQ010_2"     =>  VARS::post("SEQ010_2"),     //総合所見の項目名
                                 "SEQ008"       =>  VARS::post("SEQ008"),       //行動の記録
                                 "DBNAME"       =>  VARS::post("DBNAME")
                                );
            //帳票パターン
            $this->field2 = array("TYOUHYOU_PATTERN"      =>  VARS::post("TYOUHYOU_PATTERN"));
        } elseif ($this->pattern == 'D429') {
            $this->field = array("SEQ001"       =>  VARS::post("SEQ001"),       //帳票表題
                                 "SEQ002"       =>  VARS::post("SEQ003"),       //パターン
                                 "SEQ004"       =>  VARS::post("SEQ004"),       //クラス表示
                                 "SEQ005_1"     =>  VARS::post("SEQ005_1"),     //身体の記録
                                 "SEQ005_2"     =>  VARS::post("SEQ005_2"),     //身体の記録
                                 "SEQ006"       =>  VARS::post("SEQ006"),       //視力
                                 "SEQ007"       =>  VARS::post("SEQ007"),       //聴力
                                 "SEQ008"       =>  VARS::post("SEQ008"),       //行動の記録
                                 "SEQ009"       =>  VARS::post("SEQ009"),       //年間目標
                                 "DBNAME"       =>  VARS::post("DBNAME")
                                );
            //状態区分
            $this->field2 = array("STATUS"      =>  VARS::post("STATUS"));

            //パターン
            $this->frmPatern = VARS::post("HID_FRM_PATERN");
        } elseif ($this->pattern == 'D185E') {
            $this->field = array("SEQ001"       =>  VARS::post("SEQ001"),
                                //  "SEQ002"       =>  VARS::post("SEQ002"),
                                 "SEQ003"       =>  VARS::post("SEQ003"),
                                 "SEQ004"       =>  VARS::post("SEQ004"),
                                 "SEQ005"       =>  VARS::post("SEQ005"),
                                //  "SEQ006"       =>  VARS::post("SEQ006"),
                                 "SEQ007"       =>  VARS::post("SEQ007"),
                                 "SEQ008"       =>  VARS::post("SEQ008"),
                                 "SEQ009"       =>  VARS::post("SEQ009"),
                                 "SEQ010"       =>  VARS::post("SEQ010"),
                                 "SEQ011"       =>  VARS::post("SEQ011"),
                                 "SEQ012"       =>  VARS::post("SEQ012"),
                                 "SEQ013"       =>  VARS::post("SEQ013"),
                                 "DBNAME"       =>  VARS::post("DBNAME")
                            );
            //成績の表示項目
            for ($i=0; $i < get_count($this->semesterList); $i++) {
                $semester = $this->semesterList[$i]["SEMESTER"];
                if ($semester == $this->semesterMax) {
                    continue;
                }
                $this->field["SEQ002".$semester] = VARS::post("SEQ002".$semester);
            }
            //出欠の記録の表示項目
            for ($i=0; $i < get_count($this->semesterList); $i++) {
                $semester = $this->semesterList[$i]["SEMESTER"];
                $this->field["SEQ006".$semester] = VARS::post("SEQ006".$semester);
            }
        } elseif ($this->pattern == 'D181H' || $this->pattern == 'D181A') {
            $this->field = array("SEQ001"       =>  VARS::post("SEQ001"),
                                 "SEQ002"       =>  VARS::post("SEQ002"),
                                 "SEQ003"       =>  VARS::post("SEQ003"),
                                 "SEQ0041"      =>  VARS::post("SEQ0041"),
                                 "SEQ0042"      =>  VARS::post("SEQ0042"),
                                 "SEQ005"       =>  VARS::post("SEQ005"),
                                 "SEQ006"       =>  VARS::post("SEQ006"),
                                 "DBNAME"       =>  VARS::post("DBNAME")
                            );
        } elseif ($this->pattern == 'D186V') {
            $this->field = array("SEQ001"       =>  VARS::post("SEQ001"),
                                 "SEQ002"       =>  VARS::post("SEQ002"),
                                 "SEQ003"       =>  VARS::post("SEQ003"),
                                 "SEQ004"       =>  VARS::post("SEQ004"),
                                 "SEQ005"       =>  VARS::post("SEQ005"),
                                 "SEQ006"       =>  VARS::post("SEQ006"),
                                 "SEQ007"       =>  VARS::post("SEQ007"),
                                 "SEQ008"       =>  VARS::post("SEQ008"),
                                 "SEQ009"       =>  VARS::post("SEQ009"),
                                 "SEQ010"       =>  VARS::post("SEQ010"),
                                 "SEQ0111"      =>  VARS::post("SEQ0111"),
                                 "SEQ0112"      =>  VARS::post("SEQ0112"),
                                 "SEQ012"       =>  VARS::post("SEQ012"),
                                 "SEQ013"       =>  VARS::post("SEQ013"),
                                 "SEQ014"       =>  VARS::post("SEQ014"),
                                 "SEQ015"       =>  VARS::post("SEQ015"),
                                 "SEQ016"       =>  VARS::post("SEQ016"),
                                 "SEQ017"       =>  VARS::post("SEQ017"),
                                 "SEQ018"       =>  VARS::post("SEQ018"),
                                 "SEQ019"       =>  VARS::post("SEQ019"),
                                 "SEQ020"       =>  VARS::post("SEQ020"),
                                 "DBNAME"       =>  VARS::post("DBNAME")
                            );
        } else {
            $this->field = array("SEQ001"       =>  VARS::post("SEQ001"),
                                 "SEQ002"       =>  VARS::post("SEQ002"),
                                 "SEQ003"       =>  VARS::post("SEQ003"),
                                 "SEQ004"       =>  VARS::post("SEQ004"),
                                 "SEQ005"       =>  VARS::post("SEQ005"),
                                 "SEQ0061"      =>  VARS::post("SEQ0061"),
                                 "SEQ0062"      =>  VARS::post("SEQ0062"),
                                 "SEQ0063"      =>  VARS::post("SEQ0063"),
                                 "SEQ0064"      =>  VARS::post("SEQ0064"),
                                 "SEQ007"       =>  VARS::post("SEQ007"),
                                 "SEQ008"       =>  VARS::post("SEQ008"),
                                 "SEQ009"       =>  VARS::post("SEQ009"),
                                 "SEQ010"       =>  VARS::post("SEQ010"),
                                 "SEQ011"       =>  VARS::post("SEQ011"),
                                 "SEQ012"       =>  VARS::post("SEQ012"),
                                 "SEQ013"       =>  VARS::post("SEQ013"),
                                 "SEQ014"       =>  VARS::post("SEQ014"),
                                 "SEQ0151"      =>  VARS::post("SEQ0151"),
                                 "SEQ0152"      =>  VARS::post("SEQ0152"),
                                 "SEQ016"       =>  VARS::post("SEQ016"),
                                 "SEQ017"       =>  VARS::post("SEQ017"),
                                 "SEQ018"       =>  VARS::post("SEQ018"),
                                 "SEQ019"       =>  VARS::post("SEQ019"),
                                 "SEQ020"       =>  VARS::post("SEQ020"),
                                 "SEQ021"       =>  VARS::post("SEQ021"),
                                 "SEQ022"       =>  VARS::post("SEQ022"),
                                 "DBNAME"       =>  VARS::post("DBNAME")
                            );
        }

        $this->cmd = VARS::request("cmd");

        if ($this->cmd == "changeKind") {
            unset($this->field);
        }
    }

    //入力項目チェック
    public function validateInput()
    {
        if ($this->pattern == 'D429') {
            //表題
            if (strlen($this->field["SEQ001"]) > 30) {
                $this->setWarning("MSG914", "(表題) 30バイトまで");
                return false;
            }
        } elseif ($this->pattern == 'D429BD') {
            //表題
            if ($this->field["SEQ001"] && strlen($this->field["SEQ001"]) > 30) {
                $this->setWarning("MSG914", "(表題) 30バイトまで");
                return false;
            }
            //総合所見（項目名）
            if ($this->field["SEQ010_2"] && strlen($this->field["SEQ010_2"]) > 60) {
                $this->setWarning("MSG914", "(総合所見（項目名）) 60バイトまで");
                return false;
            }
        } elseif ($this->pattern == 'D429LM') {
            //表題
            if ($this->field["SEQ001"] && strlen($this->field["SEQ001"]) > 30) {
                $this->setWarning("MSG914", "(表題) 全角10文字まで");
                return false;
            }
            //教育目標・目指す生徒像
            if (strlen($this->field["SEQ013"]) > 0) {
                if ($this->validate_row_cnt($this->field["SEQ013"], ($this->seq013_moji * 2)) > $this->seq013_gyou) {
                    $this->setWarning("MSG914", "(教育目標・目指す生徒像) 全角{$this->seq013_moji}文字X{$this->seq013_gyou}行まで");
                    return false;
                }
            }
        }
        return true;
    }

    //更新処理
    public function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()) {
            if ($this->pattern == 'D429LM') {
                $this->field["SEQ002"] = $this->namecdA035[$this->field["SEQ002"]]["value"];
            }
            knjz235wQuery::getUpdateQuery($this);
            unset($this->field);
            $this->setMessage("MSG201");
        }
    }

    //前年度からコピー
    public function &getCopyModel()
    {
        $db = Query::dbCheckOut();
        //データ件数
        $pre_year = CTRL_YEAR - 1;
        $cnt = $db->getOne(knjz235wQuery::checkExistData($this, $pre_year, "", "copy"));

        //前年度のデータがない場合、エラーを返す
        if ($cnt == 0) {
            $this->setWarning("MSG303");
            return false;
        }

        //コピー処理
        $result = $db->query(knjz235wQuery::checkExistData($this, $pre_year, "", "pre_data"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            knjz235wQuery::copyInsert($db, $this, $row);
        }
        unset($this->field);
        $this->setMessage("MSG201");

        Query::dbCheckIn($db);
    }
}

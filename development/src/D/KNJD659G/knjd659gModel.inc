<?php
/**
 * 定数定義
*/

class knjd659gModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;        //コントロールマスタ
    public $selectSchregno;
    public $schoolName;

    /**
     * メンバ変数の初期化
     */
    public function knjd659gModel()
    {
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("GRADE"            =>  VARS::post("GRADE"),            //学年
                             "HR_CLASS"         =>  VARS::post("HR_CLASS"),         //クラス
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                            );

        //DB接続
        $db = Query::dbCheckOut();

        //学校名取得
        $query = knjd659gQuery::getSchoolName();
        $this->schoolName = $db->getOne($query);

        //DB切断
        Query::dbCheckIn($db);

        $this->cmd = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }

    //CSV出力処理
    public function getDownloadModel()
    {
        //DB接続
        $db = Query::dbCheckOut();

        $this->field["GRADE"] = substr($this->field["HR_CLASS"], 0, 2);

        $semsterArray = array();
        $query = knjd659gQuery::getSemester();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $semsterArray[] =  $row["VALUE"];
        }

        //年度
        $nendo = common::DateConv1(str_replace("-", "/", CTRL_DATE), 2);

        $contents = "";

        //タイトル
        $title  = $nendo."度 ";
        $title .= "観点別成績一覧表";

        //ヘッダ（2行目）
        $header2 = ",,,,,,";

        //ヘッダ（1行目）
        $contents .= "年度,学期,学籍番号,学年,組,出席番号,生徒氏名";
        $query = knjd659gQuery::getJviewSql($this);
        $result = $db->query($query);
        $sep = ",";
        $hyoutei = "";
        $subClassCd = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //データ内にカンマがあればピリオドに置換・・・置換する文字は、カンマでなければ何でも良い。
            $row = str_replace(",", ".", $row);

            $hyoutei = "評定";
            if ($subClassCd != $row["SUBCLASSCD"]) {
                //評定列を追加
                $contents .= $sep.$row["CLASSNAME"];
                $header2 .= $sep.$hyoutei;
            }
            $contents .= $sep.$row["CLASSNAME"];
            $header2 .= $sep.$row["VIEWNAME"];

            $subClassCd = $row["SUBCLASSCD"];
        }
        $result->free();
        $contents .= "\n";
        $contents .= $header2."\n";

        //明細
        $postStudentSelected = VARS::post("HID_STUDENT_SELECTED");
        $this->selectSchregno = array();
        $selectedStudent = explode(",", $postStudentSelected);
        foreach ($selectedStudent as $val) {
            $tmp = explode("-", $val);
            $this->selectSchregno[] = "'".$tmp[0]."'";
        }

        for ($i = 0; $i < count($semsterArray); $i++) {
            $seme = $semsterArray[$i] == "9" ? CTRL_SEMESTER : $semsterArray[$i];

            //SQL文発行
            $query = knjd659gQuery::getStudentInfo($this, $semsterArray[$i]);
            $result = $db->query($query);
            $sep = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ内にカンマがあればピリオドに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",", ".", $row);

                //生徒情報
                $contents .= $row["YEAR"].",".$row["SEMESTER"].",".$row["SCHREGNO"].",".$row["GRADE"].",".$row["HR_CLASS_NAME1"].",".$row["ATTENDNO"].",".$row["NAME"];
                $sep = ",";

                //観点(明細)
                $query = knjd659gQuery::getJviewStatSql($this, $row["SCHREGNO"], $semsterArray[$i]);
                $result2 = $db->query($query);
                $subClassCd = "";
                $electdiv = "";
                while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //評定
                    if ($subClassCd != $row2["SUBCLASSCD"]) {
                        $query = knjd659gQuery::getHyoukaSql($this, $row["SCHREGNO"], $row2["SUBCLASSCD"], $semsterArray[$i]);
                        $status = $db->getOne($query);
                        if ($this->Properties["useRecordDat"] != 'KIN_RECORD_DAT') {
                            if ($row2["ELECTDIV"] == "1") {
                                $status = "";
                                if ($status == "11") {
                                    $status = "A";
                                }
                                if ($status == "22") {
                                    $status = "B";
                                }
                                if ($status == "33") {
                                    $status = "C";
                                }
                            }
                        }
                        $contents .= $sep.$status;
                    }

                    //観点
                    $contents .= $sep.$row2["STATUS"];
                    $subClassCd = $row2["SUBCLASSCD"];
                }
                $result2->free();

                $contents .= "\n";
                $data = true;
            }
            $result->free();
        }

        //DB切断
        Query::dbCheckIn($db);

        //CSV出力
        if ($data) {
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

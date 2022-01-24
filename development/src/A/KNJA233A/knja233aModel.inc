<?php

require_once('for_php7.php');

class knja233aModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
    public $schoolName;
    public $schoolCode;
    public $isTuusin;

    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限

    public function knja233aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
        $this->staffcd      = STAFFCD;                  //職員コード
    }

    public function init()
    {
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }

        //権限チェック
        $this->auth = $this->getPrgId ? $this->sendAuth : common::SecurityCheck(STAFFCD, 'KNJA233A');

        $this->getPropertiesAll(); //プロパティファイル

        $this->year = CTRL_YEAR;
        $this->field = array("SEMESTER"         =>  VARS::post("SEMESTER"),         //学期
                             "CLASSCD"          =>  VARS::post("CLASSCD"),          //教科コード
                             "SUBCLASSCD"       =>  VARS::post("SUBCLASSCD"),       //科目コード
                             "PRINT_DIV"        =>  VARS::post("PRINT_DIV"),        //帳票種別　1:講座名簿（OUTPUT=1,2,3）、2:教務手帳（OUTPUT=4）
                             "PATTERN"          =>  VARS::post("PATTERN"),          //講座名簿AB
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //名票
                             "OUTPUT4AB"        =>  VARS::post("OUTPUT4AB"),        //教務手帳AB
                             "HURIGANA_OUTPUT"  =>  VARS::post("HURIGANA_OUTPUT"),  //ふりがな出力
                             "HEIGHT"           =>  VARS::post("HEIGHT"),           //縦サイズ
                             "WIDTH"            =>  VARS::post("WIDTH"),            //横サイズ
                             "PRINT_SCHREGNO"   =>  VARS::post("PRINT_SCHREGNO"),   //学籍番号を表記する
                             "KENSUU"           =>  VARS::post("KENSUU"),           //出力件数
                             "DATE"             =>  VARS::post("DATE"),             //学籍処理日
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        if (!$this->field["DATE"]) {
            $this->field["DATE"] = $this->control["学籍処理日"];
        }

        $this->cmd = VARS::request("cmd");

        //学校名取得
        $query = knja233aQuery::getSchoolName();
        $db = Query::dbCheckOut();
        $rowZ010 = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $this->schoolName = $rowZ010["NAME1"];
        $this->schoolCode = $rowZ010["NAME2"];
        $this->isTuusin   = $db->getOne(knja233aQuery::getTuusinflg());
        Query::dbCheckIn($db);
    }

    /*******************************************************************************/
    /*******************************************************************************/
    /*******************************************************************************/
    /*******************************************************************************/
    /*******************************************************************************/
    //CSVの出力
    public function getDownloadModel()
    {
        $attendclasscd = VARS::post("ATTENDCLASSCD");
        if (isset($attendclasscd)) {
            $this->attendclasscd    = explode(',', VARS::post("ATTENDCLASSCD"));
        }

        $groupcd = VARS::post("GROUPCD");
        if (isset($groupcd)) {
            $this->groupcd    = explode(',', VARS::post("GROUPCD"));
        }

        $name_show = VARS::post("NAME_SHOW");
        if (isset($name_show)) {
            $this->name_show    = explode(',', VARS::post("NAME_SHOW"));
        }

        $chargediv = VARS::post("CHARGEDIV");
        if (isset($chargediv)) {
            $this->chargediv    = explode(',', VARS::post("CHARGEDIV"));
        }

        $appdate = VARS::post("APPDATE");
        if (isset($appdate)) {
            $this->appdate    = explode(',', VARS::post("APPDATE"));
        }

        //ヘッダの作成
        if ($this->Properties["useFormNameA233A"] == "KNJA233A_10" && $this->field["PRINT_DIV"] == 1 && $this->field["PATTERN"]  == 3 || $this->field["PATTERN"]  == 4) {
            $header = array("CHAIRNAMET"        => "講座名",
                            "CHAIRNAME"         => "講座名"
                            );
        } elseif ($this->field["OUTPUT"] == 1 || $this->field["OUTPUT"] == 'musashi') {
            $header = array("CHAIRNAMET"        => "講座名",
                            "CHAIRNAME"         => "講座名",
                            "STAFFNAMET"        => "担当者名",
                            "STAFFNAME"         => "担当者名"
                            );
        } else {
            $header = array("CHAIRCDT"          => "講座コード",
                            "CHAIRCD"           => "講座コード",
                            "CHAIRNAMET"        => "講座名",
                            "CHAIRNAME"         => "講座名",
                            "STAFFNAMET"        => "担当者名",
                            "STAFFNAME"         => "担当者名"
                            );
        }

        //項目作成
        if ($this->Properties["useFormNameA233A"] == "KNJA233A_7" && $this->field["PRINT_DIV"] == 1 && $this->field["PATTERN"]  == 3) {
            $meisai = array("SCHREGNO"          => "連番",
                            "SEX"               => "性別",
                            "NAME"              => "氏名",
                            "KUBUN"             => "区分",
                            "NAMEKANA"          => "かな",
                            "HR_CLASS"          => "年組",
                            "ATTENDNO"          => "出席番号"
                            );
        } elseif ($this->Properties["useFormNameA233A"] == "KNJA233A_10" && $this->field["PRINT_DIV"] == 1 && $this->field["PATTERN"]  == 3 || $this->field["PATTERN"]  == 4) {
            $meisai = array("HR_CLASS"          => "年組",
                            "ATTENDNO"          => "出席番号",
                            "SEX"               => "性別",
                            "NAME"              => "氏名"
                            );
        } elseif ($this->field["OUTPUT"] == 1 || $this->field["OUTPUT"] == 'musashi') {
            if ($this->field["PRINT_DIV"] == 1 && $this->field["PATTERN"] == 2) {
                $meisai = array("SCHREGNO"          => "連番",
                                "GRADE"             => "学年",
                                "HR_CLASS"          => "組",
                                "ATTENDNO"          => "出席番号",
                                "SEX"               => "性別",
                                "NAME"              => "氏名",
                                "NAMEKANA"          => "かな"
                            );
            } else {
                $meisai = array("SCHREGNO"          => "連番",
                                "SEX"               => "性別",
                                "NAME"              => "氏名",
                                "NAMEKANA"          => "かな",
                                "HR_CLASS"          => "年組",
                                "ATTENDNO"          => "出席番号"
                            );
            }
        } else {
            if ($this->field["PRINT_DIV"] == 1 && $this->field["PATTERN"] == 2) {
                if ($this->schoolName == 'hirokoudai' && $this->isTuusin == "1") {
                    $meisai = array("SCHREGNO"          => "学籍番号",
                                    "SEX"               => "性別",
                                    "NAME"              => "氏名",
                                    "TEACHER_NAME"      => "チューター",
                                    "NAMEKANA"          => "かな"
                                );
                } elseif ($this->schoolName == 'tosajoshi' || $this->schoolName == 'osakatoin') {
                    $meisai = array("HR_CLASS"          => "組",
                                    "ATTENDNO"          => "番",
                                    "SEX"               => "性別",
                                    "NAME"              => "氏名",
                                    "NAMEKANA"          => "ふりがな"
                                );
                } else {
                    $meisai = array("SCHREGNO"          => "学籍番号",
                                    "GRADE"             => "学年",
                                    "HR_CLASS"          => "組",
                                    "ATTENDNO"          => "出席番号",
                                    "SEX"               => "性別",
                                    "NAME"              => "氏名",
                                    "NAMEKANA"          => "かな"
                                );
                }
            } else {
                $meisai = array("SCHREGNO"          => "学籍番号",
                                "SEX"               => "性別",
                                "NAME"              => "氏名",
                                "NAMEKANA"          => "かな",
                                "HR_CLASS"          => "年組",
                                "ATTENDNO"          => "出席番号"
                            );
            }
        }

        $contents = "" ;

        //出力データの作成//
        for ($i = 0; $i < get_count($this->attendclasscd); $i++) {
            $renban = 1 ;
            if ($i > 0) {
                $contents .= "\n";
            }
            //講座名称取得
            $db = Query::dbCheckOut();
            $query = knja233aQuery::getCair($this->attendclasscd[$i], $this);
            $chairname = $db->getOne($query);
            Query::dbCheckIn($db);

            if ($this->field["OUTPUT"] == 1 || $this->field["OUTPUT"] == 'musashi') {
                $header["CHAIRNAME"] = $chairname;
            } else {
                $header["CHAIRCD"]   = $this->attendclasscd[$i];
                $header["CHAIRNAME"] = $chairname;
            }

            if (!($this->Properties["useFormNameA233A"] == "KNJA233A_10" && $this->field["PRINT_DIV"] == 1 && $this->field["PATTERN"]  == 3 || $this->field["PATTERN"]  == 4)) {
                //担当者名称取得
                $db = Query::dbCheckOut();
                $query = knja233aQuery::getStaff($this->name_show[$i]);
                $staffname = $db->getOne($query);
                Query::dbCheckIn($db);
                $header["STAFFNAME"] = $staffname;
            }

            //職員情報取得
            $db = Query::dbCheckOut();
            $query = knja233aQuery::getStaffInfo();
            $staffInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);

            //見出し設定
            $contents .= implode($header, ",") ."\n";
            $contents .= implode($meisai, ",") ."\n";

            //DB接続
            $db = Query::dbCheckOut();
            //SQL文発行
            $query = knja233aQuery::csvDataGet($this->field["OUTPUT"], $this->attendclasscd[$i], $this->appdate[$i], $this);
            $result = $db->query($query);
            $data = false;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                $row = str_replace(",", ".", $row);

                //8:関西学院
                if ($this->Properties["useFormNameA233A"] == "KNJA233A_10" && $this->field["PRINT_DIV"] == 1 && $this->field["PATTERN"]  == 3 || $this->field["PATTERN"]  == 4) {
                    $contents .= $row["HR_NAME"].",";
                    $contents .= $row["ATTENDNO"].",";
                    $contents .= $row["SEX"].",";
                    $contents .= $row["NAME"].",";
                } else {
                    if ($this->Properties["useFormNameA233A"] == "KNJA233A_7" && $this->field["PRINT_DIV"] == 1 && $this->field["PATTERN"]  == 3) {
                        $contents .= $renban.",";
                    } elseif ($this->field["OUTPUT"] == 1 || $this->field["OUTPUT"] == 'musashi') {
                        $contents .= $renban.",";
                    } elseif ($this->schoolName != 'tosajoshi' && $this->schoolName != 'osakatoin') {
                        $contents .= $row["SCHREGNO"].",";
                    }
                    if ($this->field["PRINT_DIV"] == 1 && $this->field["PATTERN"] == 2) {
                        if ($this->schoolName == 'hirokoudai' && $this->isTuusin == "1") {
                        } elseif ($this->schoolName == 'tosajoshi' || $this->schoolName == 'osakatoin') {
                            $contents .= $row["HR_CLASS"].",";
                            $contents .= $row["ATTENDNO"].",";
                        } else {
                            $contents .= $row["GRADE"].",".$row["HR_CLASS"].",";
                            $contents .= $row["ATTENDNO"].",";
                        }
                    }
                    $contents .= $row["SEX"].",";
                    $contents .= $row["NAME"].",";
                    if ($this->Properties["useFormNameA233A"] == "KNJA233A_7" && $this->field["PRINT_DIV"] == 1 && $this->field["PATTERN"]  == 3) {
                        $contents .= $row["KUBUN"].",";
                    }
                    if ($this->field["PRINT_DIV"] == 1 && $this->field["PATTERN"] == 2) {
                        if ($this->schoolName == 'hirokoudai' && $this->isTuusin == "1") {
                            $contents .= $row["TEACHER_NAME"].",";
                        }
                    }
                    $jpOrEngName = $staffInfo["FIELD5"] == '1' ? $row["NAME_ENG"] : $row["NAME_KANA"];
                    $contents .= $jpOrEngName.",";
                    if ($this->field["PRINT_DIV"] == 1 && $this->field["PATTERN"] == 2) {
                    } else {
                        $contents .= $row["HR_NAMEABBV"].",";
                        $contents .= $row["ATTENDNO"].",";
                    }
                }

                $contents .= "\n";
                $data = true;
                $renban++;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        if ($data) {
            if ($this->field["OUTPUT"] == 2) {
                $couse = "講座別名票(学籍番号順)";
            } elseif ($this->field["OUTPUT"] == 'musashi') {
                $couse = "講座別名票";
            } else {
                $couse = "講座別名票(年組番号順)";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }

    public function clean()
    {
    }
}

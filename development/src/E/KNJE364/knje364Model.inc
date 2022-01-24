<?php

require_once('for_php7.php');
/**
 * 定数定義
*/
class knje364Model extends Model
{
    public $field = array();
    public $data = array();
    public $cmd;
    public $file;
    public $control;
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
     */
    public function knje364Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }

        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        $this->field = array("OUTPUT"           =>  VARS::post("OUTPUT"),               //出力取込種別
                             "YEAR"             =>  VARS::post("YEAR"),                 //年度一覧
                             "COURSE_KIND"      =>  VARS::post("COURSE_KIND"),          //進路種別
                             "QUESTIONNAIRECD"  =>  VARS::post("QUESTIONNAIRECD"),      //調査名
                             "HEADER"           =>  VARS::post("HEADER"),               //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")             //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        $this->getPropertiesAll();      //プロパティファイル
        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    public function &getMainModel()
    {
    }

    /**
     * 実行モデルを返します
    */
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
                if ($this->field["SHORI_MEI"] == "1") {
                    knje364Query::insertQueryCsv($this, $this->data_arr);
                } else {
                    knje364Query::updateQueryCsv($this, $this->data_arr);
                }
                //エラー件数取得
                $this->setMessage("MSG202", "更新件数".get_count($this->data_arr)."件");
            }
        }
    }

    //取込
    public function getFileString()
    {
        $temp_arr = array();
        if (!is_uploaded_file($this->file["tmp_name"])) {        //HTTP POSTによりアップロードされたファイルかどうかを調べる
            $this->warning = "ファイル名が不正です。";
        } else {
            if ($this->field["COURSE_KIND"] == "1") {
                //進学
                $checkdata = array("YEAR"               => array(4,"年度"),
                                   "SCHREGNO"           => array(8,"学籍番号"),
                                   "ENTRYDATE"          => array(10,"登録日"),
                                   "SEQ"                => array(10,"連番"),
                                   "COURSE_KIND"        => array(1,"進路種別"),
                                   "QUESTIONNAIRECD"    => array(2,"調査コード"),
                                   "SCHOOL_GROUP1"      => array(2,"第一希望・学校系列"),
                                   "FACULTY_GROUP1"     => array(3,"第一希望・学部系列"),
                                   "DEPARTMENT_GROUP1"  => array(3,"第一希望・学科系列"),
                                   "SCHOOL_CD1"         => array(8,"第一希望・学校コード"),
                                   "FACULTYCD1"         => array(3,"第一希望・学部コード"),
                                   "DEPARTMENTCD1"      => array(3,"第一希望・学科コード"),
                                   "HOWTOEXAM1"         => array(10,"第一希望・受験区分"),
                                   "SCHOOL_GROUP2"      => array(2,"第二希望・学校系列"),
                                   "FACULTY_GROUP2"     => array(3,"第二希望・学部系列"),
                                   "DEPARTMENT_GROUP2"  => array(3,"第二希望・学科系列"),
                                   "SCHOOL_CD2"         => array(8,"第二希望・学校コード"),
                                   "FACULTYCD2"         => array(3,"第二希望・学部コード"),
                                   "DEPARTMENTCD2"      => array(3,"第二希望・学科コード"),
                                   "HOWTOEXAM2"         => array(10,"第二希望・受験区分"),
                                   "SCHOOL_GROUP3"      => array(2,"第三希望・学校系列"),
                                   "FACULTY_GROUP3"     => array(3,"第三希望・学部系列"),
                                   "DEPARTMENT_GROUP3"  => array(3,"第三希望・学科系列"),
                                   "SCHOOL_CD3"         => array(8,"第三希望・学校コード"),
                                   "FACULTYCD3"         => array(3,"第三希望・学部コード"),
                                   "DEPARTMENTCD3"      => array(3,"第三希望・学科コード"),
                                   "HOWTOEXAM3"         => array(10,"第三希望・受験区分"),
                                   "REMARK"             => array(300,"備考"));
            } elseif ($this->field["COURSE_KIND"] == "2") {
                //就職
                $checkdata = array("YEAR"               => array(4,"年度"),
                                   "SCHREGNO"           => array(8,"学籍番号"),
                                   "ENTRYDATE"          => array(10,"登録日"),
                                   "SEQ"                => array(10,"連番"),
                                   "COURSE_KIND"        => array(1,"進路種別"),
                                   "QUESTIONNAIRECD"    => array(2,"調査コード"),
                                   "JOBTYPE_LCD1"       => array(2,"第一希望・職業種別（大分類）"),
                                   "JOBTYPE_MCD1"       => array(2,"第一希望・職業種別（中分類）"),
                                   "JOBTYPE_SCD1"       => array(3,"第一希望・職業種別（小分類）"),
                                   "WORK_AREA1"         => array(1,"第一希望・就業場所"),
                                   "INTRODUCTION_DIV1"  => array(1,"第一希望・紹介区分"),
                                   "JOBTYPE_LCD2"       => array(2,"第二希望・職業種別（大分類）"),
                                   "JOBTYPE_MCD2"       => array(2,"第二希望・職業種別（中分類）"),
                                   "JOBTYPE_SCD2"       => array(3,"第二希望・職業種別（小分類）"),
                                   "WORK_AREA2"         => array(1,"第二希望・就業場所"),
                                   "INTRODUCTION_DIV2"  => array(1,"第二希望・紹介区分"),
                                   "REMARK"             => array(300,"備考"));
            } else {
                //その他、未定
                $checkdata = array("YEAR"               => array(4,"年度"),
                                   "SCHREGNO"           => array(8,"学籍番号"),
                                   "ENTRYDATE"          => array(10,"登録日"),
                                   "SEQ"                => array(10,"連番"),
                                   "COURSE_KIND"        => array(1,"進路種別"),
                                   "QUESTIONNAIRECD"    => array(2,"調査コード"),
                                   "REMARK"             => array(300,"備考"));
            }

            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            //エラーデータの削除
            $query = $this->deleteQueryErr(PROGRAMID);
            $result = $db->query($query);

            $fp = fopen($this->file["tmp_name"], "r");
            $i = 0;
            $line = 1;  //行数
            $header = false;
            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);
                if (trim($buffer) != '') {
                    if (!$header && $this->field["HEADER"] == "on") {
                        $header = true;
                        $line++;
                        continue;
                    }
                    //文字コードを判別、改行コードを削除
                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp    = common::csv2array($buffer);

                    if ($this->field["COURSE_KIND"] == "1") {
                        //進学
                        $data = array("YEAR"                => (strlen(trim($tmp[0]))) ? sprintf("%04d", $tmp[0]) : "",
                                      "SCHREGNO"            => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                      "ENTRYDATE"           => $tmp[6],
                                      "SEQ"                 => $tmp[7],
                                      "COURSE_KIND"         => $tmp[8],
                                      "QUESTIONNAIRECD"     => $tmp[10],
                                      "SCHOOL_GROUP1"       => (strlen(trim($tmp[12]))) ? sprintf("%02d", $tmp[12]) : "",
                                      "FACULTY_GROUP1"      => (strlen(trim($tmp[14]))) ? sprintf("%03d", $tmp[14]) : "",
                                      "DEPARTMENT_GROUP1"   => (strlen(trim($tmp[16]))) ? sprintf("%03d", $tmp[16]) : "",
                                      "SCHOOL_CD1"          => (strlen(trim($tmp[18]))) ? sprintf("%08d", $tmp[18]) : "",
                                      "FACULTYCD1"          => (strlen(trim($tmp[20]))) ? sprintf("%03d", $tmp[20]) : "",
                                      "DEPARTMENTCD1"       => (strlen(trim($tmp[22]))) ? sprintf("%03d", $tmp[22]) : "",
                                      "HOWTOEXAM1"          => $tmp[24],
                                      "SCHOOL_GROUP2"       => (strlen(trim($tmp[26]))) ? sprintf("%02d", $tmp[26]) : "",
                                      "FACULTY_GROUP2"      => (strlen(trim($tmp[28]))) ? sprintf("%03d", $tmp[28]) : "",
                                      "DEPARTMENT_GROUP2"   => (strlen(trim($tmp[30]))) ? sprintf("%03d", $tmp[30]) : "",
                                      "SCHOOL_CD2"          => (strlen(trim($tmp[32]))) ? sprintf("%08d", $tmp[32]) : "",
                                      "FACULTYCD2"          => (strlen(trim($tmp[34]))) ? sprintf("%03d", $tmp[34]) : "",
                                      "DEPARTMENTCD2"       => (strlen(trim($tmp[36]))) ? sprintf("%03d", $tmp[36]) : "",
                                      "HOWTOEXAM2"          => $tmp[38],
                                      "SCHOOL_GROUP3"       => (strlen(trim($tmp[40]))) ? sprintf("%02d", $tmp[40]) : "",
                                      "FACULTY_GROUP3"      => (strlen(trim($tmp[42]))) ? sprintf("%03d", $tmp[42]) : "",
                                      "DEPARTMENT_GROUP3"   => (strlen(trim($tmp[44]))) ? sprintf("%03d", $tmp[44]) : "",
                                      "SCHOOL_CD3"          => (strlen(trim($tmp[46]))) ? sprintf("%08d", $tmp[46]) : "",
                                      "FACULTYCD3"          => (strlen(trim($tmp[48]))) ? sprintf("%03d", $tmp[48]) : "",
                                      "DEPARTMENTCD3"       => (strlen(trim($tmp[50]))) ? sprintf("%03d", $tmp[50]) : "",
                                      "HOWTOEXAM3"          => $tmp[52],
                                      "REMARK"              => $tmp[54]);
                    } elseif ($this->field["COURSE_KIND"] == "2") {
                        //就職
                        $data = array("YEAR"                => (strlen(trim($tmp[0]))) ? sprintf("%04d", $tmp[0]) : "",
                                      "SCHREGNO"            => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                      "ENTRYDATE"           => $tmp[6],
                                      "SEQ"                 => $tmp[7],
                                      "COURSE_KIND"         => $tmp[8],
                                      "QUESTIONNAIRECD"     => $tmp[10],
                                      "JOBTYPE_LCD1"        => $tmp[12],
                                      "JOBTYPE_MCD1"        => (strlen(trim($tmp[14]))) ? sprintf("%02d", $tmp[14]) : "",
                                      "JOBTYPE_SCD1"        => (strlen(trim($tmp[16]))) ? sprintf("%03d", $tmp[16]) : "",
                                      "WORK_AREA1"          => $tmp[18],
                                      "INTRODUCTION_DIV1"   => $tmp[20],
                                      "JOBTYPE_LCD2"        => $tmp[22],
                                      "JOBTYPE_MCD2"        => (strlen(trim($tmp[24]))) ? sprintf("%02d", $tmp[24]) : "",
                                      "JOBTYPE_SCD2"        => (strlen(trim($tmp[26]))) ? sprintf("%03d", $tmp[26]) : "",
                                      "WORK_AREA2"          => $tmp[28],
                                      "INTRODUCTION_DIV2"   => $tmp[30],
                                      "REMARK"              => $tmp[32]);
                    } else {
                        //その他、未定
                        $data = array("YEAR"                => (strlen(trim($tmp[0]))) ? sprintf("%04d", $tmp[0]) : "",
                                      "SCHREGNO"            => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                      "ENTRYDATE"           => $tmp[6],
                                      "SEQ"                 => $tmp[7],
                                      "COURSE_KIND"         => $tmp[8],
                                      "QUESTIONNAIRECD"     => $tmp[10],
                                      "REMARK"              => $tmp[12]);
                    }

                    //エラーチェック
                    if ($this->errCheck($db, $data, $checkdata, $line)) {
                        //ハイフンに変換してセット
                        if ($data["ENTRYDATE"] != "") {
                            $data["ENTRYDATE"] = $this->dateRep($data["ENTRYDATE"]);
                        }

                        if ($data["REMARK"] != "") {
                            $data["REMARK"] = str_replace("↓", "\r\n", $data["REMARK"]);
                        }

                        $temp_arr[$i] = $data;
                        $i++;
                    }
                    $line++;
                }
            }
            $db->commit(); // トランザクションをコミットする。
            Query::dbCheckIn($db);
            fclose($fp);
        }

        return $temp_arr;
    }

    //エラーチェック
    public function errCheck($db, $data, $data_each, $line)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $dateErrFlg      = false;      //日付エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $dateErrMsg      = "";       //日付エラーメッセージ

        $hissuSep     = "";
        $existenceSep = "";
        $numericSep   = "";
        $lengthSep    = "";
        $dateSep      = "";

        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if ($this->field["COURSE_KIND"] == "1") {
                $hissu_array = array("YEAR", "SCHREGNO", "ENTRYDATE", "COURSE_KIND", "QUESTIONNAIRECD", "SCHOOL_GROUP1");
            } elseif ($this->field["COURSE_KIND"] == "2") {
                $hissu_array = array("YEAR", "SCHREGNO", "ENTRYDATE", "COURSE_KIND", "QUESTIONNAIRECD", "INTRODUCTION_DIV1", "INTRODUCTION_DIV2");
            } else {
                $hissu_array = array("YEAR", "SCHREGNO", "ENTRYDATE", "COURSE_KIND", "QUESTIONNAIRECD");
            }
            if (in_array($key, $hissu_array)) {
                if (!strlen(trim($data[$key]))) {
                    $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                }
            }
            if ($this->field["SHORI_MEI"] == "2") {
                //必須項目チェック
                if (in_array($key, array("SEQ"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    } else {
                        //更新データ有無
                        if (1 > $db->getOne(knje364Query::updDataExists($data["ENTRYDATE"], $data[$key], $data["SCHREGNO"]))) {
                            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                        }
                    }
                }
            } else {
                //連番不要チェック
                if (in_array($key, array("SEQ"))) {
                    if (strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "新規選択時は".$val[1]."は空白");
                    }
                }
            }
            if (in_array($key, array("ENTRYDATE"))) {
                if ($data[$key] != "" && $this->dateRep($data[$key]) == "") {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付フォーマット(斜線またはハイフン区切り):", $dateSep, $val[1]);
                }
            }

            //数値チェック
            if ($this->field["COURSE_KIND"] == "1") {
                $num_array = array("YEAR","SCHREGNO", "SEQ",  "COURSE_KIND", "QUESTIONNAIRECD", "SCHOOL_GROUP1", "FACULTY_GROUP1", "DEPARTMENT_GROUP1", "SCHOOL_CD1", "FACULTYCD1", "DEPARTMENTCD1", "HOWTOEXAM1", "SCHOOL_GROUP2", "FACULTY_GROUP2", "DEPARTMENT_GROUP2", "SCHOOL_CD2", "FACULTYCD2", "DEPARTMENTCD2", "HOWTOEXAM2");
            } elseif ($this->field["COURSE_KIND"] == "2") {
                $num_array = array("YEAR","SCHREGNO", "SEQ",  "COURSE_KIND", "QUESTIONNAIRECD", "JOBTYPE_MCD1", "JOBTYPE_SCD1", "WORK_AREA1", "INTRODUCTION_DIV1", "JOBTYPE_MCD2", "JOBTYPE_SCD2", "WORK_AREA2", "INTRODUCTION_DIV2");
            } else {
                $num_array = array("YEAR","SCHREGNO", "SEQ",  "COURSE_KIND", "QUESTIONNAIRECD");
            }
            if (in_array($key, $num_array)) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }

            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }

            //存在チェック
            $query = "";
            if ($key == "QUESTIONNAIRECD") {
                $query = knje364Query::chkQuestionnaireMst($data["YEAR"], $data[$key]);
            }
            if ($key == "SCHOOL_GROUP1" || $key == "SCHOOL_GROUP2") {
                $query = knje364Query::chkNameMst($data["YEAR"], 'E012', $data[$key]);
            }
            if ($key == "FACULTY_GROUP1" || $key == "FACULTY_GROUP2") {
                $query = knje364Query::chkFacultyGroupMst($data[$key]);
            }
            if ($key == "DEPARTMENT_GROUP1" || $key == "DEPARTMENT_GROUP2") {
                $query = knje364Query::chkFacultyGroupMst($data[$key]);
            }
            if ($key == "SCHOOL_CD1" || $key == "SCHOOL_CD2") {
                $query = knje364Query::chkCollegeMst($data[$key]);
            }
            if ($key == "FACULTYCD1") {
                $query = knje364Query::chkCollegeFacultyMst($data["SCHOOL_CD1"], $data[$key]);
            }
            if ($key == "FACULTYCD2") {
                $query = knje364Query::chkCollegeFacultyMst($data["SCHOOL_CD2"], $data[$key]);
            }
            if ($key == "DEPARTMENTCD1") {
                $query = knje364Query::chkCollegeDepartmentMst($data["SCHOOL_CD1"], $data["FACULTYCD1"], $data[$key]);
            }
            if ($key == "DEPARTMENTCD2") {
                $query = knje364Query::chkCollegeDepartmentMst($data["SCHOOL_CD2"], $data["FACULTYCD2"], $data[$key]);
            }
            if ($key == "HOWTOEXAM1" || $key == "HOWTOEXAM2") {
                $query = knje364Query::chkNameMst($data["YEAR"], 'E002', $data[$key], 1);
            }
            if ($key == "JOBTYPE_LCD1" || $key == "JOBTYPE_LCD2") {
                $query = knje364Query::chkJobtypeLMst($data[$key]);
            }
            if ($key == "JOBTYPE_MCD1") {
                $query = knje364Query::chkJobtypeMMst($data["JOBTYPE_LCD1"], $data[$key]);
            }
            if ($key == "JOBTYPE_MCD2") {
                $query = knje364Query::chkJobtypeMMst($data["JOBTYPE_LCD2"], $data[$key]);
            }
            if ($key == "JOBTYPE_SCD1") {
                $query = knje364Query::chkJobtypeSMst($data["JOBTYPE_LCD1"], $data["JOBTYPE_MCD1"], $data[$key]);
            }
            if ($key == "JOBTYPE_SCD2") {
                $query = knje364Query::chkJobtypeSMst($data["JOBTYPE_LCD2"], $data["JOBTYPE_MCD2"], $data[$key]);
            }
            if ($key == "SCHOOL_CD3" && $this->field["COURSE_KIND"] == "1") {
                $query = knje364Query::chkCollegeMst($data[$key]);
            }
            if ($query) {
                if (strlen(trim($data[$key])) && $db->getOne($query) == 0) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }

            if (in_array($key, array("COURSE_KIND"))) {
                if (strlen(trim($data[$key])) && $data[$key] != $this->field["COURSE_KIND"]) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }
            if (in_array($key, array("WORK_AREA1", "WORK_AREA2"))) {
                if (strlen(trim($data[$key])) && !in_array($data[$key], array(1, 2))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }
            if (in_array($key, array("INTRODUCTION_DIV1", "INTRODUCTION_DIV2"))) {
                if (strlen(trim($data[$key])) && !in_array($data[$key], array(1, 2, 3))) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }
        }

        if (!$returnFlg) {
            $checkError = $hissuErrMsg.$existenceErrMsg.$numericErrMsg.$lengthErrMsg.$dateErrMsg;
            $checkError = $this->checkErrLength($checkError);
            knje364Query::insertQueryErr($db, $line, $checkError);
        }
        return $returnFlg;
    }

    //データの整合性チェック
    public function isDataMatched(&$data_arr)
    {
        //５千件を超えたら制限エラー
        if (get_count($data_arr) > 5000) {
            $this->setWarning("MSG915", "処理件数を５千件以下にして下さい 処理件数：".get_count($data_arr)."件");
            return false;
        }
        return true;
    }

    //ＣＳＶ出力
    public function getDownloadModel()
    {
        //ヘッダ項目（エラー出力）
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        if ($this->field["COURSE_KIND"] == "1") {
            //ヘッダ項目（進学）
            $header = array("YEAR"                      => "※年度",
                            "GRADE"                     => "学年",
                            "HR_CLASS"                  => "クラス",
                            "ATTENDNO"                  => "出席番号",
                            "SCHREGNO"                  => "※学籍番号",
                            "NAME"                      => "氏名",
                            "ENTRYDATE"                 => "※登録日",
                            "SEQ"                       => "連番",
                            "COURSE_KIND"               => "※進路種別",
                            "COURSE_KIND_NAME"          => "進路種別名称",
                            "QUESTIONNAIRECD"           => "※調査コード",
                            "QUESTIONNAIRENAME"         => "調査名称",
                            "SCHOOL_GROUP1"             => "※第一希望・学校系列",
                            "SCHOOL_GROUP1_NAME"        => "第一希望・学校系列名称",
                            "FACULTY_GROUP1"            => "第一希望・学部系列",
                            "FACULTY_GROUP1_NAME"       => "第一希望・学部系列名称",
                            "DEPARTMENT_GROUP1"         => "第一希望・学科系列",
                            "DEPARTMENT_GROUP1_NAME"    => "第一希望・学科系列名称",
                            "SCHOOL_CD1"                => "第一希望・学校コード",
                            "SCHOOL_NAME1"              => "第一希望・学校名称",
                            "FACULTYCD1"                => "第一希望・学部コード",
                            "FACULTYNAME1"              => "第一希望・学部名称",
                            "DEPARTMENTCD1"             => "第一希望・学科コード",
                            "DEPARTMENTNAME1"           => "第一希望・学科名称",
                            "HOWTOEXAM1"                => "第一希望・受験区分",
                            "HOWTOEXAM1_NAME"           => "第一希望・受験区分名称",
                            "SCHOOL_GROUP2"             => "第二希望・学校系列",
                            "SCHOOL_GROUP2_NAME"        => "第二希望・学校系列名称",
                            "FACULTY_GROUP2"            => "第二希望・学部系列",
                            "FACULTY_GROUP2_NAME"       => "第二希望・学部系列名称",
                            "DEPARTMENT_GROUP2"         => "第二希望・学科系列",
                            "DEPARTMENT_GROUP2_NAME"    => "第二希望・学科系列名称",
                            "SCHOOL_CD2"                => "第二希望・学校コード",
                            "SCHOOL_NAME2"              => "第二希望・学校名称",
                            "FACULTYCD2"                => "第二希望・学部コード",
                            "FACULTYNAME2"              => "第二希望・学部名称",
                            "DEPARTMENTCD2"             => "第二希望・学科コード",
                            "DEPARTMENTNAME2"           => "第二希望・学科名称",
                            "HOWTOEXAM2"                => "第二希望・受験区分",
                            "HOWTOEXAM2_NAME"           => "第二希望・受験区分名称",
                            "SCHOOL_GROUP3"             => "第三希望・学校系列",
                            "SCHOOL_GROUP3_NAME"        => "第三希望・学校系列名称",
                            "FACULTY_GROUP3"            => "第三希望・学部系列",
                            "FACULTY_GROUP3_NAME"       => "第三希望・学部系列名称",
                            "DEPARTMENT_GROUP3"         => "第三希望・学科系列",
                            "DEPARTMENT_GROUP3_NAME"    => "第三希望・学科系列名称",
                            "SCHOOL_CD3"                => "第三希望・学校コード",
                            "SCHOOL_NAME3"              => "第三希望・学校名称",
                            "FACULTYCD3"                => "第三希望・学部コード",
                            "FACULTYNAME3"              => "第三希望・学部名称",
                            "DEPARTMENTCD3"             => "第三希望・学科コード",
                            "DEPARTMENTNAME3"           => "第三希望・学科名称",
                            "HOWTOEXAM3"                => "第三希望・受験区分",
                            "HOWTOEXAM3_NAME"           => "第三希望・受験区分名称",
                            "REMARK"                    => "備考",
                            $this->lastColumn           => $this->lastColumn);

            //ヘッダ例（進学）
            $header_rei = array("YEAR"                      => "年度",
                                "GRADE"                     => "99",
                                "HR_CLASS"                  => "999",
                                "ATTENDNO"                  => "999",
                                "SCHREGNO"                  => "12345678",
                                "NAME"                      => "アルプ太郎",
                                "ENTRYDATE"                 => str_replace("-", "/", CTRL_DATE),
                                "SEQ"                       => "連番",
                                "COURSE_KIND"               => "1",
                                "COURSE_KIND_NAME"          => "進学",
                                "QUESTIONNAIRECD"           => "99",
                                "QUESTIONNAIRENAME"         => "調査名称",
                                "SCHOOL_GROUP1"             => "99",
                                "SCHOOL_GROUP1_NAME"        => "第一希望・学校系列名称",
                                "FACULTY_GROUP1"            => "999",
                                "FACULTY_GROUP1_NAME"       => "第一希望・学部系列名称",
                                "DEPARTMENT_GROUP1"         => "999",
                                "DEPARTMENT_GROUP1_NAME"    => "第一希望・学科系列名称",
                                "SCHOOL_CD1"                => "99999999",
                                "SCHOOL_NAME1"              => "第一希望・学校名称",
                                "FACULTYCD1"                => "999",
                                "FACULTYNAME1"              => "第一希望・学部名称",
                                "DEPARTMENTCD1"             => "999",
                                "DEPARTMENTNAME1"           => "第一希望・学科名称",
                                "HOWTOEXAM1"                => "9999",
                                "HOWTOEXAM1_NAME"           => "第一希望・受験区分名称",
                                "SCHOOL_GROUP2"             => "99",
                                "SCHOOL_GROUP2_NAME"        => "第二希望・学校系列名称",
                                "FACULTY_GROUP2"            => "999",
                                "FACULTY_GROUP2_NAME"       => "第二希望・学部系列名称",
                                "DEPARTMENT_GROUP2"         => "999",
                                "DEPARTMENT_GROUP2_NAME"    => "第二希望・学科系列名称",
                                "SCHOOL_CD2"                => "99999999",
                                "SCHOOL_NAME2"              => "第二希望・学校名称",
                                "FACULTYCD2"                => "999",
                                "FACULTYNAME2"              => "第二希望・学部名称",
                                "DEPARTMENTCD2"             => "999",
                                "DEPARTMENTNAME2"           => "第二希望・学科名称",
                                "HOWTOEXAM2"                => "9999",
                                "HOWTOEXAM2_NAME"           => "第二希望・受験区分名称",
                                "SCHOOL_GROUP3"             => "99",
                                "SCHOOL_GROUP3_NAME"        => "第三希望・学校系列名称",
                                "FACULTY_GROUP3"            => "999",
                                "FACULTY_GROUP3_NAME"       => "第三希望・学部系列名称",
                                "DEPARTMENT_GROUP3"         => "999",
                                "DEPARTMENT_GROUP3_NAME"    => "第三希望・学科系列名称",
                                "SCHOOL_CD3"                => "99999999",
                                "SCHOOL_NAME3"              => "第三希望・学校名称",
                                "FACULTYCD3"                => "999",
                                "FACULTYNAME3"              => "第三希望・学部名称",
                                "DEPARTMENTCD3"             => "999",
                                "DEPARTMENTNAME3"           => "第三希望・学科名称",
                                "HOWTOEXAM3"                => "9999",
                                "HOWTOEXAM3_NAME"           => "第三希望・受験区分名称",
                                "REMARK"                    => "全角100文字",
                                $this->lastColumn           => $this->lastColumn);
        } elseif ($this->field["COURSE_KIND"] == "2") {
            //ヘッダ項目（就職）
            $header = array("YEAR"                      => "※年度",
                            "GRADE"                     => "学年",
                            "HR_CLASS"                  => "クラス",
                            "ATTENDNO"                  => "出席番号",
                            "SCHREGNO"                  => "※学籍番号",
                            "NAME"                      => "氏名",
                            "ENTRYDATE"                 => "※登録日",
                            "SEQ"                       => "連番",
                            "COURSE_KIND"               => "※進路種別",
                            "COURSE_KIND_NAME"          => "進路種別名称",
                            "QUESTIONNAIRECD"           => "※調査コード",
                            "QUESTIONNAIRENAME"         => "調査名称",
                            "JOBTYPE_LCD1"              => "第一希望・職業種別（大分類）",
                            "JOBTYPE_LCD1_NAME"         => "第一希望・職業種別（大分類）名称",
                            "JOBTYPE_MCD1"              => "第一希望・職業種別（中分類）",
                            "JOBTYPE_MCD1_NAME"         => "第一希望・職業種別（中分類）名称",
                            "JOBTYPE_SCD1"              => "第一希望・職業種別（小分類）",
                            "JOBTYPE_SCD1_NAME"         => "第一希望・職業種別（小分類）名称",
                            "WORK_AREA1"                => "第一希望・就業場所",
                            "WORK_AREA1_NAME"           => "第一希望・就業場所名称",
                            "INTRODUCTION_DIV1"         => "※第一希望・紹介区分",
                            "INTRODUCTION_DIV1_NAME"    => "第一希望・紹介区分名称",
                            "JOBTYPE_LCD2"              => "第二希望・職業種別（大分類）",
                            "JOBTYPE_LCD2_NAME"         => "第二希望・職業種別（大分類）名称",
                            "JOBTYPE_MCD2"              => "第二希望・職業種別（中分類）",
                            "JOBTYPE_MCD2_NAME"         => "第二希望・職業種別（中分類）名称",
                            "JOBTYPE_SCD2"              => "第二希望・職業種別（小分類）",
                            "JOBTYPE_SCD2_NAME"         => "第二希望・職業種別（小分類）名称",
                            "WORK_AREA2"                => "第二希望・就業場所",
                            "WORK_AREA2_NAME"           => "第二希望・就業場所名称",
                            "INTRODUCTION_DIV2"         => "※第二希望・紹介区分",
                            "INTRODUCTION_DIV2_NAME"    => "第二希望・紹介区分名称",
                            "REMARK"                    => "備考",
                            $this->lastColumn           => $this->lastColumn);

            //ヘッダ項目（就職）
            $header_rei = array("YEAR"                      => "年度",
                                "GRADE"                     => "99",
                                "HR_CLASS"                  => "999",
                                "ATTENDNO"                  => "999",
                                "SCHREGNO"                  => "12345678",
                                "NAME"                      => "アルプ太郎",
                                "ENTRYDATE"                 => str_replace("-", "/", CTRL_DATE),
                                "SEQ"                       => "連番",
                                "COURSE_KIND"               => "2",
                                "COURSE_KIND_NAME"          => "就職",
                                "QUESTIONNAIRECD"           => "99",
                                "QUESTIONNAIRENAME"         => "調査名称",
                                "JOBTYPE_LCD1"              => "99",
                                "JOBTYPE_LCD1_NAME"         => "第一希望・職業種別（大分類）名称",
                                "JOBTYPE_MCD1"              => "99",
                                "JOBTYPE_MCD1_NAME"         => "第一希望・職業種別（中分類）名称",
                                "JOBTYPE_SCD1"              => "999",
                                "JOBTYPE_SCD1_NAME"         => "第一希望・職業種別（小分類）名称",
                                "WORK_AREA1"                => "1",
                                "WORK_AREA1_NAME"           => "第一希望・就業場所名称",
                                "INTRODUCTION_DIV1"         => "1～3",
                                "INTRODUCTION_DIV1_NAME"    => "第一希望・紹介区分名称",
                                "JOBTYPE_LCD2"              => "99",
                                "JOBTYPE_LCD2_NAME"         => "第二希望・職業種別（大分類）名称",
                                "JOBTYPE_MCD2"              => "99",
                                "JOBTYPE_MCD2_NAME"         => "第二希望・職業種別（中分類）名称",
                                "JOBTYPE_SCD2"              => "999",
                                "JOBTYPE_SCD2_NAME"         => "第二希望・職業種別（小分類）名称",
                                "WORK_AREA2"                => "1",
                                "WORK_AREA2_NAME"           => "第二希望・就業場所名称",
                                "INTRODUCTION_DIV2"         => "1～3",
                                "INTRODUCTION_DIV2_NAME"    => "第二希望・紹介区分名称",
                                "REMARK"                    => "全角100文字",
                                $this->lastColumn           => $this->lastColumn);
        } else {
            //ヘッダ項目（その他、未定）
            $header = array("YEAR"                  => "※年度",
                            "GRADE"                 => "学年",
                            "HR_CLASS"              => "クラス",
                            "ATTENDNO"              => "出席番号",
                            "SCHREGNO"              => "※学籍番号",
                            "NAME"                  => "氏名",
                            "ENTRYDATE"             => "※登録日",
                            "SEQ"                   => "連番",
                            "COURSE_KIND"           => "※進路種別",
                            "COURSE_KIND_NAME"      => "進路種別名称",
                            "QUESTIONNAIRECD"       => "※調査コード",
                            "QUESTIONNAIRENAME"     => "調査名称",
                            "REMARK"                => "備考",
                            $this->lastColumn       => $this->lastColumn);

            $course_kind_name = "";
            if ($this->field["COURSE_KIND"] == "3") {
                $course_kind_name = 'その他（家事手伝い等）';
            }
            if ($this->field["COURSE_KIND"] == "4") {
                $course_kind_name = '未定';
            }

            //ヘッダ項目（その他、未定）
            $header_rei = array("YEAR"                  => "年度",
                                "GRADE"                 => "99",
                                "HR_CLASS"              => "999",
                                "ATTENDNO"              => "999",
                                "SCHREGNO"              => "12345678",
                                "NAME"                  => "アルプ太郎",
                                "ENTRYDATE"             => str_replace("-", "/", CTRL_DATE),
                                "SEQ"                   => "連番",
                                "COURSE_KIND"           => $this->field["COURSE_KIND"],
                                "COURSE_KIND_NAME"      => $course_kind_name,
                                "QUESTIONNAIRECD"       => "99",
                                "QUESTIONNAIRENAME"     => "調査名称",
                                "REMARK"                => "全角100文字",
                                $this->lastColumn       => $this->lastColumn);
        }

        $contents  = "";    //クリア

        $header_data = false;   //出力フラグ
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ
            $header_data = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            //データ
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"] == "3") {
                //エラー
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ
                $query = knje364Query::selectMainQuery($this);
            }

            //SQL文発行
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($this->field["OUTPUT"] == "4") {    //データ
                    $data = array();

                    $row = str_replace(",", ".", $row);   //データ内にカンマがあればコロンに置換・・・置換する文字は、カンマでなければ何でも良い。
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);

                    if ($row["ENTRYDATE"]) {
                        $row["ENTRYDATE"] = str_replace("-", "/", $row["ENTRYDATE"]);
                    }

                    foreach ($header as $key => $val) {
                        $data[$key] = $row[$key];
                    }
                    $contents .= implode($data, ",") ."\n";
                } else {
                    $contents .= implode($row, ",") ."\n";
                }
                $header_data = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        //ＣＳＶ出力
        if ($header_data) {
            if ($this->field["OUTPUT"] == "3") {    //エラー
                $couse = "エラー出力";
            } else {                                //エラー以外
                $couse = "進路相談";
            }
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->warning = "該当データは、ありません。";
            return false;
        }
    }

    /**
     * 日付変換 項目ﾁｪｯｸ後に行う。
     */
    public function dateRep($data)
    {
        if ($data != "") {
            $data = common::DateConv1(str_replace("-", "/", $data), 4);
            if ($data) {
                $data = str_replace("/", "-", $data);
            }
        }
        return $data;
    }
}

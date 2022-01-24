<?php

require_once('for_php7.php');
class knje364aModel extends Model
{
    public $field = array();
    public $data = array();
    public $cmd;
    public $file;
    public $schoolCd;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限
    public $lastColumn;//CSVの最後の列の値

    public function knje364aModel()
    {
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
                             "QUESTIONNAIRECD"  =>  VARS::post("QUESTIONNAIRECD"),      //調査名
                             "GRADE"            =>  VARS::post("GRADE"),                //学年
                             "HR_CLASS"         =>  VARS::post("HR_CLASS"),             //クラス
                             "COURSE"           =>  VARS::post("COURSE"),               //コース
                             "SCHREGNO"         =>  VARS::post("SCHREGNO"),             //学籍番号
                             "HEADER"           =>  VARS::post("HEADER"),               //ヘッダ有
                             "SHORI_MEI"        =>  VARS::post("SHORI_MEI")             //処理名
                             );

        $this->cmd = VARS::request("cmd");
        $this->file = VARS::file("FILE");

        $this->getPropertiesAll();      //プロパティファイル
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        $this->lastColumn = 'DUMMY';
        if ($this->Properties["csv_LastColumn"] != '') {
            $this->lastColumn = $this->Properties["csv_LastColumn"];
        }
    }

    /****************/
    /*  ＣＳＶ取込  */
    /****************/
    public function &getExecModel()
    {
        //ファイルの文字列の取得
        $this->data_arr = array();
        $this->data_arr = $this->getFileString();

        //データの整合性チェック
        if ($this->isDataMatched($this->data_arr)) {
            $errFlg = $this->checkCsvErrQuery(PROGRAMID);
            if ($errFlg) {
            //更新
                knje364aQuery::updateQueryCsv($this, $this->data_arr);
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
            $checkdata0 = array("YEAR"              => array(4,"年度"),
                                "SCHREGNO"          => array(8,"学籍番号"),
                                "QUESTIONNAIRECD"   => array(2,"調査コード"),
                                "ENTRYDATE"         => array(10,"登録日"));

            $checkdata1 = array("COURSE_KIND1"      => array(1,"(進学)進路種別"),
                                "SEQ1"              => array(10,"(進学)連番"),
                                "SCHOOL_GROUP1"     => array(2,"第一希望・学校系列"),
                                "FACULTY_GROUP1"    => array(3,"第一希望・学部系列"),
                                "DEPARTMENT_GROUP1" => array(3,"第一希望・学科系列"),
                                "SCHOOL_CD1"        => array(8,"第一希望・学校コード"),
                                "FACULTYCD1"        => array(3,"第一希望・学部コード"),
                                "DEPARTMENTCD1"     => array(3,"第一希望・学科コード"),
                                "HOWTOEXAM1"        => array(10,"第一希望・受験区分"),
                                "SCHOOL_GROUP2"     => array(2,"第二希望・学校系列"),
                                "FACULTY_GROUP2"    => array(3,"第二希望・学部系列"),
                                "DEPARTMENT_GROUP2" => array(3,"第二希望・学科系列"),
                                "SCHOOL_CD2"        => array(8,"第二希望・学校コード"),
                                "FACULTYCD2"        => array(3,"第二希望・学部コード"),
                                "DEPARTMENTCD2"     => array(3,"第二希望・学科コード"),
                                "HOWTOEXAM2"        => array(10,"第二希望・受験区分"),
                                "SCHOOL_GROUP3"     => array(2,"第三希望・学校系列"),
                                "FACULTY_GROUP3"    => array(3,"第三希望・学部系列"),
                                "DEPARTMENT_GROUPv" => array(3,"第三希望・学科系列"),
                                "SCHOOL_CD3"        => array(8,"第三希望・学校コード"),
                                "FACULTYCD3"        => array(3,"第三希望・学部コード"),
                                "DEPARTMENTCD3"     => array(3,"第三希望・学科コード"),
                                "HOWTOEXAM3"        => array(10,"第三希望・受験区分"),
                                "REMARK1"           => array(300,"(進学)備考"));

            $checkdata2 = array("COURSE_KIND2"      => array(1,"(就職)進路種別"),
                                "SEQ2"              => array(10,"(就職)連番"),
                                "JOBTYPE_LCD1"      => array(2,"第一希望・職業種別（大分類）"),
                                "JOBTYPE_MCD1"      => array(2,"第一希望・職業種別（中分類）"),
                                "JOBTYPE_SCD1"      => array(3,"第一希望・職業種別（小分類）"),
                                "WORK_AREA1"        => array(1,"第一希望・就業場所"),
                                "INTRODUCTION_DIV1" => array(1,"第一希望・紹介区分"),
                                "JOBTYPE_LCD2"      => array(2,"第二希望・職業種別（大分類）"),
                                "JOBTYPE_MCD2"      => array(2,"第二希望・職業種別（中分類）"),
                                "JOBTYPE_SCD2"      => array(3,"第二希望・職業種別（小分類）"),
                                "WORK_AREA2"        => array(1,"第二希望・就業場所"),
                                "INTRODUCTION_DIV2" => array(1,"第二希望・紹介区分"),
                                "REMARK2"           => array(300,"(就職)備考"));

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

                    $data = array("YEAR"                => (strlen(trim($tmp[0]))) ? sprintf("%04d", $tmp[0]) : "",
                                  "SCHREGNO"            => (strlen(trim($tmp[4]))) ? sprintf("%08d", $tmp[4]) : "",
                                  "QUESTIONNAIRECD"     => $tmp[6],
                                  "ENTRYDATE"           => $tmp[8],
                                  "COURSE_KIND1"        => $tmp[9],
                                  "SEQ1"                => $tmp[11],
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
                                  "REMARK1"             => $tmp[54],
                                  "COURSE_KIND2"        => $tmp[55],
                                  "SEQ2"                => $tmp[57],
                                  "JOBTYPE_LCD1"        => $tmp[58],
                                  "JOBTYPE_MCD1"        => (strlen(trim($tmp[60]))) ? sprintf("%02d", $tmp[60]) : "",
                                  "JOBTYPE_SCD1"        => (strlen(trim($tmp[62]))) ? sprintf("%03d", $tmp[62]) : "",
                                  "WORK_AREA1"          => $tmp[64],
                                  "INTRODUCTION_DIV1"   => $tmp[66],
                                  "JOBTYPE_LCD2"        => $tmp[68],
                                  "JOBTYPE_MCD2"        => (strlen(trim($tmp[70]))) ? sprintf("%02d", $tmp[70]) : "",
                                  "JOBTYPE_SCD2"        => (strlen(trim($tmp[72]))) ? sprintf("%03d", $tmp[72]) : "",
                                  "WORK_AREA2"          => $tmp[74],
                                  "INTRODUCTION_DIV2"   => $tmp[76],
                                  "REMARK2"             => $tmp[78]);

                    //エラーチェック
                    $this->err[$i] = array();
                    $this->err[$i][0] = $this->errCheck0($db, $data, $checkdata0, $line);
                    $this->err[$i][1] = $this->errCheck1($db, $data, $checkdata1, $line, "1");
                    $this->err[$i][2] = $this->errCheck1($db, $data, $checkdata2, $line, "2");

                    //データの存在チェック
                    $returnFlg = true;
                    $existenceErrMsg = "";
                    if ((strlen($this->err[$i][1]["data"].$this->err[$i][2]["data"]) < 1) && (strlen($data["COURSE_KIND1"]) < 1) && (strlen($data["COURSE_KIND2"]) < 1)) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "データなし");
                    }

                    // エラーメッセージの項目名リスト
                    $errMsgList = array("hissu", "exist", "num", "len", "date");

                    if (!$this->err[$i][0]["flg"] || !$this->err[$i][1]["flg"] || !$this->err[$i][2]["flg"] || !$returnFlg) {
                        $checkError = "";
                        foreach ($errMsgList as $val) {
                            $temp_msg = "";
                            for ($j = 0; $j <= 2; $j++) {
                                $temp_msg .= $this->err[$i][$j][$val];
                                if ($val == "exist" && $j == 2) {
                                    $temp_msg .= $existenceErrMsg;
                                }
                            }
                            $checkError .= $temp_msg;
                        }
                        $checkError = $this->checkErrLength($checkError);
                        knje364aQuery::insertQueryErr($db, $line, $checkError);
                    } else {
                        //ハイフンに変換してセット
                        if ($data["ENTRYDATE"] != "") {
                            $data["ENTRYDATE"] = $this->dateRep($data["ENTRYDATE"]);
                        }
                        //改行コードを変換
                        if ($data["REMARK1"] != "") {
                            $data["REMARK1"] = str_replace("↓", "\r\n", $data["REMARK1"]);
                        }
                        if ($data["REMARK2"] != "") {
                            $data["REMARK2"] = str_replace("↓", "\r\n", $data["REMARK2"]);
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

    //エラーチェック（生徒情報）
    public function errCheck0($db, $data, $data_each, $line)
    {
        $returnFlg          = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $existenceErrFlg    = false;    //存在エラーフラグ
        $numericErrFlg      = false;    //数字エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $dateErrFlg         = false;    //日付エラーフラグ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ
        $numericErrMsg      = "";       //数字エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $dateErrMsg         = "";       //日付エラーメッセージ

        $hissuSep           = "";
        $existenceSep       = "";
        $numericSep         = "";
        $lengthSep          = "";
        $dateSep            = "";
        foreach ($data_each as $key => $val) {
            //必須項目チェック
            if (!strlen(trim($data[$key]))) {
                $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
            }
            //日付のフォーマットチェック
            if (in_array($key, array("ENTRYDATE"))) {
                if ($data[$key] != "" && $this->dateRep($data[$key]) == "") {
                    $returnFlg = $this->errSet($dateErrFlg, $dateErrMsg, "※日付フォーマット(斜線またはハイフン区切り):", $dateSep, $val[1]);
                }
            }
            //数値チェック
            if (in_array($key, array("YEAR", "SCHREGNO", "QUESTIONNAIRECD"))) {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
            //存在チェック
            if (in_array($key, array("SCHREGNO", "QUESTIONNAIRECD"))) {
                $query = "";
                if ($key == "SCHREGNO") {
                    $query = knje364aQuery::chkRegdDat($data["YEAR"], $data[$key]);
                }
                if ($key == "QUESTIONNAIRECD") {
                    $query = knje364aQuery::chkQuestionnaireMst($data["YEAR"], $data[$key]);
                }

                if ($query) {
                    if (strlen(trim($data[$key])) && $db->getOne($query) == 0) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
            }
        }

        $error = array();
        $error["flg"]   = $returnFlg;
        $error["hissu"] = $hissuErrMsg;
        $error["exist"] = $existenceErrMsg;
        $error["num"]   = $numericErrMsg;
        $error["len"]   = $lengthErrMsg;
        $error["date"]  = $dateErrMsg;

        return $error;
    }

    //エラーチェック（進学情報 / 就職情報）
    public function errCheck1($db, $data, $data_each, $line, $kind)
    {
        $returnFlg = true;
        $hissuErrFlg     = false;    //必須エラーフラグ
        $existenceErrFlg = false;    //存在エラーフラグ
        $numericErrFlg   = false;    //数字エラーフラグ
        $lengthErrFlg    = false;    //レングスエラーフラグ
        $dateErrFlg      = false;    //日付エラーフラグ

        $hissuErrMsg     = "";       //必須エラーメッセージ
        $existenceErrMsg = "";       //存在エラーメッセージ
        $numericErrMsg   = "";       //数字エラーメッセージ
        $lengthErrMsg    = "";       //レングスエラーメッセージ
        $dateErrMsg      = "";       //日付エラーメッセージ

        $hissuSep        = "";
        $existenceSep    = "";
        $numericSep      = "";
        $lengthSep       = "";
        $dateSep         = "";

        $data_exist = "";
        foreach ($data_each as $key => $val) {
            $keyname = substr($key, 0, -1);
            $subno = substr($key, -1);

            //データのNULLチェック用
            if ($this->field["SHORI_MEI"] == "1") {
                if (!in_array($key, array("COURSE_KIND".$kind, "SEQ".$kind))) {
                    $data_exist .= $data[$key];
                }
            } else {
                if ($key != "COURSE_KIND".$kind) {
                    $data_exist .= $data[$key];
                }
            }

            //数値チェック
            if ($key != "REMARK".$kind && $keyname != "JOBTYPE_LCD") {
                if ($data[$key] != "" && !is_numeric($data[$key])) {
                    $returnFlg = $this->errSet($numericErrFlg, $numericErrMsg, "※数値:", $numericSep, $val[1]);
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
            //存在チェック
            if ($key == "COURSE_KIND".$kind) {
                if (strlen(trim($data[$key])) && trim($data[$key]) != $kind) {
                    $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                }
            }
            //更新データ存在チェック（更新のみ）
            if ($this->field["SHORI_MEI"] == "2") {
                if ($key == "SEQ".$kind && $this->dateRep($data["ENTRYDATE"]) != "") {
                    if (strlen(trim($data["COURSE_KIND".$kind])) && !strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    } elseif (strlen(trim($data["COURSE_KIND".$kind])) && strlen(trim($data[$key])) && 1 > $db->getOne(knje364aQuery::updDataExists($data["ENTRYDATE"], $data[$key], $data["SCHREGNO"], $data["COURSE_KIND".$kind]))) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
            }
            //存在チェック
            if (!in_array($key, array("COURSE_KIND".$kind, "SEQ".$kind, "REMARK".$kind))) {
                $query = "";
                if ($kind == "1") {
                    if ($keyname == "SCHOOL_GROUP") {
                        $query = knje364aQuery::chkNameMst($data["YEAR"], 'E012', $data[$key]);
                    }
                    if ($keyname == "FACULTY_GROUP") {
                        $query = knje364aQuery::chkFacultyGroupMst($data[$key]);
                    }
                    if ($keyname == "DEPARTMENT_GROUP") {
                        $query = knje364aQuery::chkDepartmentGroupMst($data[$key]);
                    }
                    if ($keyname == "SCHOOL_CD") {
                        $query = knje364aQuery::chkCollegeMst($data[$key]);
                    }
                    if ($keyname == "FACULTYCD") {
                        $query = knje364aQuery::chkCollegeFacultyMst($data["SCHOOL_CD".$subno], $data[$key]);
                    }
                    if ($keyname == "DEPARTMENTCD") {
                        $query = knje364aQuery::chkCollegeDepartmentMst($data["SCHOOL_CD".$subno], $data["FACULTYCD".$subno], $data[$key]);
                    }
                    if ($keyname == "HOWTOEXAM") {
                        $query = knje364aQuery::chkNameMst($data["YEAR"], 'E002', $data[$key], 1);
                    }
                } else {
                    if ($keyname == "JOBTYPE_LCD") {
                        $query = knje364aQuery::chkJobtypeLMst($data[$key]);
                    }
                    if ($keyname == "JOBTYPE_MCD") {
                        $query = knje364aQuery::chkJobtypeMMst($data["JOBTYPE_LCD".$subno], $data[$key]);
                    }
                    if ($keyname == "JOBTYPE_SCD") {
                        $query = knje364aQuery::chkJobtypeSMst($data["JOBTYPE_LCD".$subno], $data["JOBTYPE_MCD".$subno], $data[$key]);
                    }
                }

                if ($query) {
                    if (strlen(trim($data[$key])) && $db->getOne($query) == 0) {
                        $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, $val[1]);
                    }
                }
            }
        }

        //存在チェック
        $kind_name = ($kind == "1") ? "進学" : "就職";
        if (strlen(trim($data["COURSE_KIND".$kind])) && strlen($data_exist) == 0) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "(".$kind_name.")データ");
        }
        if (!strlen(trim($data["COURSE_KIND".$kind])) && strlen($data_exist) > 0) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "(".$kind_name.")進路種別");
        }

        $error = array();
        $error["flg"]   = $returnFlg;
        $error["hissu"] = $hissuErrMsg;
        $error["exist"] = $existenceErrMsg;
        $error["num"]   = $numericErrMsg;
        $error["len"]   = $lengthErrMsg;
        $error["date"]  = $dateErrMsg;
        $error["data"]  = $data_exist;

        return $error;
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

    /****************/
    /*  ＣＳＶ出力  */
    /****************/
    public function getDownloadModel()
    {
        //ヘッダ項目（エラー出力）
        $header_err = array("MSGROW"        => "行番号",
                            "MSGREMARK"     => "メッセージ");

        //ヘッダ項目（生徒情報）
        $header0 = array("YEAR"                     => "※年度",
                         "GRADE"                    => "学年",
                         "HR_CLASS"                 => "クラス",
                         "ATTENDNO"                 => "出席番号",
                         "SCHREGNO"                 => "※学籍番号",
                         "NAME"                     => "氏名",
                         "QUESTIONNAIRECD"          => "※調査コード",
                         "QUESTIONNAIRENAME"        => "調査名称",
                         "ENTRYDATE"                => "※登録日"
                        );

        //ヘッダ項目（進学情報）
        $header1 = array("COURSE_KIND1"             => "(進学)進路種別",
                         "COURSE_KIND_NAME1"        => "(進学)進路種別名称",
                         "SEQ1"                     => "(進学)連番",
                         "SCHOOL_GROUP1"            => "第一希望・学校系列",
                         "SCHOOL_GROUP1_NAME"       => "第一希望・学校系列名称",
                         "FACULTY_GROUP1"           => "第一希望・学部系列",
                         "FACULTY_GROUP1_NAME"      => "第一希望・学部系列名称",
                         "DEPARTMENT_GROUP1"        => "第一希望・学科系列",
                         "DEPARTMENT_GROUP1_NAME"   => "第一希望・学科系列名称",
                         "SCHOOL_CD1"               => "第一希望・学校コード",
                         "SCHOOL_NAME1"             => "第一希望・学校名称",
                         "FACULTYCD1"               => "第一希望・学部コード",
                         "FACULTYNAME1"             => "第一希望・学部名称",
                         "DEPARTMENTCD1"            => "第一希望・学科コード",
                         "DEPARTMENTNAME1"          => "第一希望・学科名称",
                         "HOWTOEXAM1"               => "第一希望・受験区分",
                         "HOWTOEXAM1_NAME"          => "第一希望・受験区分名称",
                         "SCHOOL_GROUP2"            => "第二希望・学校系列",
                         "SCHOOL_GROUP2_NAME"       => "第二希望・学校系列名称",
                         "FACULTY_GROUP2"           => "第二希望・学部系列",
                         "FACULTY_GROUP2_NAME"      => "第二希望・学部系列名称",
                         "DEPARTMENT_GROUP2"        => "第二希望・学科系列",
                         "DEPARTMENT_GROUP2_NAME"   => "第二希望・学科系列名称",
                         "SCHOOL_CD2"               => "第二希望・学校コード",
                         "SCHOOL_NAME2"             => "第二希望・学校名称",
                         "FACULTYCD2"               => "第二希望・学部コード",
                         "FACULTYNAME2"             => "第二希望・学部名称",
                         "DEPARTMENTCD2"            => "第二希望・学科コード",
                         "DEPARTMENTNAME2"          => "第二希望・学科名称",
                         "HOWTOEXAM2"               => "第二希望・受験区分",
                         "HOWTOEXAM2_NAME"          => "第二希望・受験区分名称",
                         "SCHOOL_GROUP3"            => "第三希望・学校系列",
                         "SCHOOL_GROUP3_NAME"       => "第三希望・学校系列名称",
                         "FACULTY_GROUP3"           => "第三希望・学部系列",
                         "FACULTY_GROUP3_NAME"      => "第三希望・学部系列名称",
                         "DEPARTMENT_GROUP3"        => "第三希望・学科系列",
                         "DEPARTMENT_GROUP3_NAME"   => "第三希望・学科系列名称",
                         "SCHOOL_CD3"               => "第三希望・学校コード",
                         "SCHOOL_NAME3"             => "第三希望・学校名称",
                         "FACULTYCD3"               => "第三希望・学部コード",
                         "FACULTYNAME3"             => "第三希望・学部名称",
                         "DEPARTMENTCD3"            => "第三希望・学科コード",
                         "DEPARTMENTNAME3"          => "第三希望・学科名称",
                         "HOWTOEXAM3"               => "第三希望・受験区分",
                         "HOWTOEXAM3_NAME"          => "第三希望・受験区分名称",
                         "REMARK1"                  => "(進学)備考"
                        );

        //ヘッダ項目（就職情報）
        $header2 = array("COURSE_KIND2"             => "(就職)進路種別",
                         "COURSE_KIND_NAME2"        => "(就職)進路種別名称",
                         "SEQ2"                     => "(就職)連番",
                         "JOBTYPE_LCD1"             => "第一希望・職業種別（大分類）",
                         "JOBTYPE_LCD1_NAME"        => "第一希望・職業種別（大分類）名称",
                         "JOBTYPE_MCD1"             => "第一希望・職業種別（中分類）",
                         "JOBTYPE_MCD1_NAME"        => "第一希望・職業種別（中分類）名称",
                         "JOBTYPE_SCD1"             => "第一希望・職業種別（小分類）",
                         "JOBTYPE_SCD1_NAME"        => "第一希望・職業種別（小分類）名称",
                         "WORK_AREA1"               => "第一希望・就業場所",
                         "WORK_AREA1_NAME"          => "第一希望・就業場所名称",
                         "INTRODUCTION_DIV1"        => "第一希望・紹介区分",
                         "INTRODUCTION_DIV1_NAME"   => "第一希望・紹介区分名称",
                         "JOBTYPE_LCD2"             => "第二希望・職業種別（大分類）",
                         "JOBTYPE_LCD2_NAME"        => "第二希望・職業種別（大分類）名称",
                         "JOBTYPE_MCD2"             => "第二希望・職業種別（中分類）",
                         "JOBTYPE_MCD2_NAME"        => "第二希望・職業種別（中分類）名称",
                         "JOBTYPE_SCD2"             => "第二希望・職業種別（小分類）",
                         "JOBTYPE_SCD2_NAME"        => "第二希望・職業種別（小分類）名称",
                         "WORK_AREA2"               => "第二希望・就業場所",
                         "WORK_AREA2_NAME"          => "第二希望・就業場所名称",
                         "INTRODUCTION_DIV2"        => "第二希望・紹介区分",
                         "INTRODUCTION_DIV2_NAME"   => "第二希望・紹介区分名称",
                         "REMARK2"                  => "(就職)備考",
                         $this->lastColumn          => $this->lastColumn
                         );

        //ヘッダ例（生徒情報）
        $header_rei0 = array("YEAR"                      => "年度",
                            "GRADE"                     => "99",
                            "HR_CLASS"                  => "999",
                            "ATTENDNO"                  => "999",
                            "SCHREGNO"                  => "12345678",
                            "NAME"                      => "アルプ太郎",
                            "QUESTIONNAIRECD"           => "99",
                            "QUESTIONNAIRENAME"         => "調査名称",
                            "ENTRYDATE"                 => str_replace("-", "/", CTRL_DATE)
                            );

        //ヘッダ例（進学情報）
        $header_rei1 = array("COURSE_KIND1"             => "1",
                             "COURSE_KIND_NAME1"        => "進学",
                             "SEQ1"                     => "1",
                             "SCHOOL_GROUP1"            => "99",
                             "SCHOOL_GROUP1_NAME"       => "第一希望・学校系列名称",
                             "FACULTY_GROUP1"           => "999",
                             "FACULTY_GROUP1_NAME"      => "第一希望・学部系列名称",
                             "DEPARTMENT_GROUP1"        => "999",
                             "DEPARTMENT_GROUP1_NAME"   => "第一希望・学科系列名称",
                             "SCHOOL_CD1"               => "99999999",
                             "SCHOOL_NAME1"             => "第一希望・学校名称",
                             "FACULTYCD1"               => "999",
                             "FACULTYNAME1"             => "第一希望・学部名称",
                             "DEPARTMENTCD1"            => "999",
                             "DEPARTMENTNAME1"          => "第一希望・学科名称",
                             "HOWTOEXAM1"               => "9999",
                             "HOWTOEXAM1_NAME"          => "第一希望・受験区分名称",
                             "SCHOOL_GROUP2"            => "99",
                             "SCHOOL_GROUP2_NAME"       => "第二希望・学校系列名称",
                             "FACULTY_GROUP2"           => "999",
                             "FACULTY_GROUP2_NAME"      => "第二希望・学部系列名称",
                             "DEPARTMENT_GROUP2"        => "999",
                             "DEPARTMENT_GROUP2_NAME"   => "第二希望・学科系列名称",
                             "SCHOOL_CD2"               => "99999999",
                             "SCHOOL_NAME2"             => "第二希望・学校名称",
                             "FACULTYCD2"               => "999",
                             "FACULTYNAME2"             => "第二希望・学部名称",
                             "DEPARTMENTCD2"            => "999",
                             "DEPARTMENTNAME2"          => "第二希望・学科名称",
                             "HOWTOEXAM2"               => "9999",
                             "HOWTOEXAM2_NAME"          => "第二希望・受験区分名称",
                             "SCHOOL_GROUP3"            => "99",
                             "SCHOOL_GROUP3_NAME"       => "第三希望・学校系列名称",
                             "FACULTY_GROUP3"           => "999",
                             "FACULTY_GROUP3_NAME"      => "第三希望・学部系列名称",
                             "DEPARTMENT_GROUP3"        => "999",
                             "DEPARTMENT_GROUP3_NAME"   => "第三希望・学科系列名称",
                             "SCHOOL_CD3"               => "99999999",
                             "SCHOOL_NAME3"             => "第三希望・学校名称",
                             "FACULTYCD3"               => "999",
                             "FACULTYNAME3"             => "第三希望・学部名称",
                             "DEPARTMENTCD3"            => "999",
                             "DEPARTMENTNAME3"          => "第三希望・学科名称",
                             "HOWTOEXAM3"               => "9999",
                             "HOWTOEXAM3_NAME"          => "第三希望・受験区分名称",
                             "REMARK1"                  => "全角100文字",
                            );

        //ヘッダ例（就職情報）
        $header_rei2 = array("COURSE_KIND2"             => "2",
                             "COURSE_KIND_NAME2"        => "就職",
                             "SEQ2"                     => "2",
                             "JOBTYPE_LCD1"             => "99",
                             "JOBTYPE_LCD1_NAME"        => "第一希望・職業種別（大分類）名称",
                             "JOBTYPE_MCD1"             => "99",
                             "JOBTYPE_MCD1_NAME"        => "第一希望・職業種別（中分類）名称",
                             "JOBTYPE_SCD1"             => "999",
                             "JOBTYPE_SCD1_NAME"        => "第一希望・職業種別（小分類）名称",
                             "WORK_AREA1"               => "1",
                             "WORK_AREA1_NAME"          => "第一希望・就業場所名称",
                             "INTRODUCTION_DIV1"        => "1～3",
                             "INTRODUCTION_DIV1_NAME"   => "第一希望・紹介区分名称",
                             "JOBTYPE_LCD2"             => "99",
                             "JOBTYPE_LCD2_NAME"        => "第二希望・職業種別（大分類）名称",
                             "JOBTYPE_MCD2"             => "99",
                             "JOBTYPE_MCD2_NAME"        => "第二希望・職業種別（中分類）名称",
                             "JOBTYPE_SCD2"             => "999",
                             "JOBTYPE_SCD2_NAME"        => "第二希望・職業種別（小分類）名称",
                             "WORK_AREA2"               => "1",
                             "WORK_AREA2_NAME"          => "第二希望・就業場所名称",
                             "INTRODUCTION_DIV2"        => "1～3",
                             "INTRODUCTION_DIV2_NAME"   => "第二希望・紹介区分名称",
                             "REMARK2"                  => "全角100文字",
                             $this->lastColumn          => $this->lastColumn
                            );

        $contents   = "";       //クリア
        $csv_output = false;    //出力フラグ
        $header     = array_merge($header0, $header1, $header2);
        $header_rei = array_merge($header_rei0, $header_rei1, $header_rei2);
        if ($this->field["OUTPUT"] == "1") {
            //ヘッダ出力
            $csv_output = true;
            $contents .= implode($header, ",")."\n";
            $contents .= implode($header_rei, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "3") {
            //エラー出力
            $contents .= implode($header_err, ",")."\n";
        } elseif ($this->field["OUTPUT"] == "4") {
            //データ出力
            if ($this->field["HEADER"] == "on") {
                $contents .= implode($header, ",")."\n";
            }
        }

        //ヘッダ出力以外
        if ($this->field["OUTPUT"] != "1") {
            //DB接続
            $db = Query::dbCheckOut();

            if ($this->field["OUTPUT"] == "3") {
                //エラー出力
                $query = $this->selectCsvErrQuery(PROGRAMID);
            } elseif ($this->field["OUTPUT"] == "4") {
                //データ出力
                $query = knje364aQuery::selectMainQuery($this);
            }

            //SQL文発行
            $data_tmp0 = $data_tmp1 = $data_tmp2 = array();
            $tmp = "";
            $line = 0;
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //データ出力
                if ($this->field["OUTPUT"] == "4") {
                    //カンマを置換
                    $row = str_replace(",", ".", $row);
                    //改行コードを置換
                    $row = str_replace("\r\n", "↓", $row);
                    $row = str_replace("\r", "↓", $row);
                    $row = str_replace("\n", "↓", $row);

                    if ($row["ENTRYDATE"]) {
                        $row["ENTRYDATE"] = str_replace("-", "/", $row["ENTRYDATE"]);
                    }

                    $row["COURSE_KIND".$row["COURSE_KIND"]] = $row["COURSE_KIND"];
                    $row["COURSE_KIND_NAME".$row["COURSE_KIND"]] = $row["COURSE_KIND_NAME"];
                    $row["SEQ".$row["COURSE_KIND"]] = $row["SEQ"];
                    $row["REMARK".$row["COURSE_KIND"]] = $row["REMARK"];

                    if ($row["SCHREGNO"].":".$row["ENTRYDATE"].":".$row["COURSE_KIND"] == $tmp) {
                        $line++;
                    } else {
                        //生徒情報等格納
                        $data_tmp0["data"][$row["SCHREGNO"].":".$row["ENTRYDATE"]] = $row;
                        $line = 0;
                    }
                    //MAX行数格納
                    if ($data_tmp0["cnt"][$row["SCHREGNO"].":".$row["ENTRYDATE"]] <= $line) {
                        $data_tmp0["cnt"][$row["SCHREGNO"].":".$row["ENTRYDATE"]] = $line;
                    }

                    if ($row["COURSE_KIND"] == "1") {
                        //進学情報格納
                        $data_tmp1[$row["SCHREGNO"].":".$row["ENTRYDATE"]][$line] = $row;
                    } elseif ($row["COURSE_KIND"] == "2") {
                        //就職情報格納
                        $data_tmp2[$row["SCHREGNO"].":".$row["ENTRYDATE"]][$line] = $row;
                    }

                    $tmp = $row["SCHREGNO"].":".$row["ENTRYDATE"].":".$row["COURSE_KIND"];

                //エラー出力
                } else {
                    $contents .= implode($row, ",") ."\n";
                }
                $csv_output = true;
            }
            $result->free();
            Query::dbCheckIn($db);
        }

        if ($this->field["OUTPUT"] == "4" && $csv_output) {
            foreach ($data_tmp0["cnt"] as $key => $val) {
                for ($i = 0; $i <= $val; $i++) {
                    //生徒情報
                    foreach ($header0 as $k => $v) {
                        $contents .= $data_tmp0["data"][$key][$k].",";
                    }
                    //進学情報
                    foreach ($header1 as $k => $v) {
                        $contents .= $data_tmp1[$key][$i][$k].",";
                    }
                    //就職情報
                    foreach ($header2 as $k => $v) {
                        if ($k == $this->lastColumn) {
                            $contents .= $this->lastColumn."\n";
                        } else {
                            $contents .= $data_tmp2[$key][$i][$k].",";
                        }
                    }
                }
            }
        }

        //ＣＳＶ出力
        if ($csv_output) {
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

    //日付変換 項目ﾁｪｯｸ後に行う。
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

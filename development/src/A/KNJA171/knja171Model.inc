<?php

require_once('for_php7.php');
/**
 * 定数定義
*/

class knja171Model extends Model
{
    public $field      = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $staffcd;   //職員コード
    public $selectdata;
    public $z010Name1;
    public $ismeikei;
    public $isFi;
    public $isGhr;
    public $isGakunenKongou;
        
    /**
     * メンバ変数の初期化
     */
    public function knja171Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    public function init()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $query = knja171Query::getZ010Name1();
        $this->z010Name1 = $db->getOne($query);
        Query::dbCheckIn($db);
        if ($this->z010Name1 == "meikei") {
            $this->ismeikei = true;
            $this->field = array("YEAR"                  =>  VARS::post("YEAR"),                  //年度
                                 "OUTPUT"                =>  VARS::post("OUTPUT"),                //学期コンボ
                                 "CHOICE"                =>  VARS::post("CHOICE"),                //出力指定
                                 "GRADE_HR_CLASS"        =>  VARS::post("GRADE_HR_CLASS"),        //クラス
                                 "TEL"                   =>  VARS::post("TEL"),                   //電話番号チェックボックス
                                 "E_TEL"                 =>  VARS::post("E_TEL"),                 //急用電話番号チェックボックス
                                 "SEX"                   =>  VARS::post("SEX"),                   //性別チェックボックス
                                 "BIRTHDAY"              =>  VARS::post("BIRTHDAY"),              //生年月日チェックボックス
                                 "FORM"                  =>  VARS::post("FORM"),                  //フォーム選択コンボ
                                 "FORM3_CLUB_CHECK"      =>  VARS::post("FORM3_CLUB_CHECK"),      //部活動印字
                                 "FORM3_GRD_CHECK"       =>  VARS::post("FORM3_GRD_CHECK"),       //出身校印字
                                 "FORM3_DORMITORY_CHECK" =>  VARS::post("FORM3_DORMITORY_CHECK"), //寮生区分印字
                                 "DBNAME"                =>  VARS::post("DBNAME")                 //DB変更に備えて
                                );
        } else {
            $this->ismeikei = false;
            $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                                 "OUTPUT"               =>  VARS::post("OUTPUT"),               //学期コンボ
                                 "CHOICE"               =>  VARS::post("CHOICE"),               //出力指定
                                 "GAKUNEN_KONGOU"       =>  VARS::post("GAKUNEN_KONGOU"),       //学年混合
                                 "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //クラス
                                 "TEL"                  =>  VARS::post("TEL"),                  //電話番号チェックボックス
                                 "E_TEL"                =>  VARS::post("E_TEL"),                //急用電話番号チェックボックス
                                 "SEX"                  =>  VARS::post("SEX"),                  //性別チェックボックス
                                 "BIRTHDAY"             =>  VARS::post("BIRTHDAY"),             //生年月日チェックボックス
                                 "FORM"                 =>  VARS::post("FORM"),                 //フォーム選択コンボ
                                 "FORM3_CLUB_CHECK"     =>  VARS::post("FORM3_CLUB_CHECK"),     //部活動印字
                                 "FORM3_GRD_CHECK"      =>  VARS::post("FORM3_GRD_CHECK"),      //出身校印字
                                 "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                                );
        }

        $this->cmd         = VARS::request("cmd");
        $this->selectdata = VARS::post("selectdata");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //生徒項目名切替処理
        $schName = "";
        $db = Query::dbCheckOut();
        //テーブルの有無チェック
        $query = knja171Query::checkTableExist();
        $table_cnt = $db->getOne($query);
        if ($table_cnt > 0 && (($this->field["CHOICE"] == "2" && $this->field["GRADE_HR_CLASS"]) || ($this->Properties["use_prg_schoolkind"] == "1"))) {
            //生徒項目名取得
            $schName = $db->getOne(knja171Query::getSchName($this));
        } elseif ($table_cnt > 0 && (($this->field["CHOICE"] == "2" && $this->field["GRADE_HR_CLASS"]) || ($this->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != ""))) {
            //生徒項目名取得
            $schName = $db->getOne(knja171Query::getSchName($this));
        }
        $this->sch_label = (strlen($schName) > 0) ? $schName : '生徒';
        $this->isFi = false;
        $this->isGhr = false;
        $this->isGakunenKongou = false;
        if ($this->Properties["dispMTokuHouJituGrdMixChkRad"] == "1") {
            if ($this->field["CHOICE"] == "3") {
                if ($this->Properties["useFi_Hrclass"] != '1' && $this->Properties["useSpecial_Support_Hrclass"] == "1") {
                    $this->isGhr = true;
                } else {
                    $this->isFi = true;
                }
            } elseif ($this->field["CHOICE"] == "1" && $this->field["GAKUNEN_KONGOU"] == "1") {
                if ($this->Properties["useSpecial_Support_Hrclass"] == "1") {
                    $this->isGakunenKongou = true;
                }
            }
        }
        Query::dbCheckIn($db);
    }

    //CSV出力処理
    public function getCsvModel()
    {
        if ($this->z010Name1 == 'sundaikoufu') {
            $schregnotext = "学籍番号";
            $schregnotext4 = "学籍番号";
            $namekanatext4 = "読み";
            $attendnotext = "出席番号";
            $finschoolnametext = SCHOOLKIND == "P" ? "出身園" : "出身学校";
        } else {
            if ($this->Properties["KNJA171_UseFixedTitile"] == "1" && in_array($this->field["FORM"], array("1", "2", "4"))) {
                $schregnotext = "学籍番号";
                $schregnotext4 = "学籍番号";
            } else {
                $schregnotext = $this->sch_label."手帳番号";
                $schregnotext4 = $this->sch_label."番号";
            }
            $namekanatext4 = "よみ";
            $attendnotext = "学級番号";
            $finschoolnametext = "出身学校";
        }
        //ヘッダ項目（出力対象項目）
        $header = array();
        if ($this->z010Name1 == 'bunkyo') {
            $header[1] = array( "HR_NAME"           => "年組",
                                "STAFFNAME"         => "担任名",
                                "SCHREGNO"          => $schregnotext,
                                "ATTENDNO"          => $attendnotext,
                                "NAME"              => "氏名",
                                "NAME_KANA"         => "ふりがな",
                                "BIRTHDAY"          => "生年月日",
                                "ZIPCD"             => "郵便番号",
                                "ADDR1"             => "現住所",
                                "ADDR2"             => "現住所２",
                                "TEL"               => "電話番号",
                                "GUARD_NAME"        => "保護者氏名",
                                "E_TEL"             => "急用電話番号",
                                "FINSCHOOL_NAME"    => $finschoolnametext
                              );

            $header[2] = array( "HR_NAME"           => "年組",
                                "STAFFNAME"         => "担任名",
                                "SCHREGNO"          => $schregnotext,
                                "ATTENDNO"          => $attendnotext,
                                "NAME"              => "氏名",
                                "NAME_KANA"         => "ふりがな",
                                "BIRTHDAY"          => "生年月日",
                                "SEX"               => "性別",
                                "ZIPCD"             => "郵便番号",
                                "ADDR1"             => "現住所",
                                "ADDR2"             => "現住所２",
                                "TEL"               => "電話番号",
                                "GUARD_NAME"        => "保護者氏名",
                                "E_TEL"             => "急用電話番号",
                                "FINSCHOOL_NAME"    => $finschoolnametext,
                                "REMARK"            => "備考"
                              );
        } elseif ($this->ismeikei && $this->field["FORM3_DORMITORY_CHECK"] == "1") {
            $header[1] = array( "HR_NAME"           => "年組",
                                "STAFFNAME"         => "担任名",
                                "SCHREGNO"          => $schregnotext,
                                "ATTENDNO"          => $attendnotext,
                                "NAME"              => "氏名",
                                "NAME_KANA"         => "ふりがな",
                                "BIRTHDAY"          => "生年月日",
                                "ZIPCD"             => "郵便番号",
                                "ADDR1"             => "現住所",
                                "TEL"               => "電話番号",
                                "GUARD_NAME"        => "保護者氏名",
                                "E_TEL"             => "急用電話番号",
                                "FINSCHOOL_NAME"    => $finschoolnametext,
                                "DORMITORYFLG"      => "寮生"
                              );

            $header[2] = array( "HR_NAME"           => "年組",
                                "STAFFNAME"         => "担任名",
                                "SCHREGNO"          => $schregnotext,
                                "ATTENDNO"          => $attendnotext,
                                "NAME"              => "氏名",
                                "NAME_KANA"         => "ふりがな",
                                "BIRTHDAY"          => "生年月日",
                                "SEX"               => "性別",
                                "ZIPCD"             => "郵便番号",
                                "ADDR1"             => "現住所",
                                "TEL"               => "電話番号",
                                "GUARD_NAME"        => "保護者氏名",
                                "E_TEL"             => "急用電話番号",
                                "FINSCHOOL_NAME"    => $finschoolnametext,
                                "REMARK"            => "備考"
                              );
        } else {
            $header[1] = array( "HR_NAME"           => "年組",
                                "STAFFNAME"         => "担任名",
                                "SCHREGNO"          => $schregnotext,
                                "ATTENDNO"          => $attendnotext,
                                "NAME"              => "氏名",
                                "NAME_KANA"         => "ふりがな",
                                "BIRTHDAY"          => "生年月日",
                                "ZIPCD"             => "郵便番号",
                                "ADDR1"             => "現住所",
                                "TEL"               => "電話番号",
                                "GUARD_NAME"        => "保護者氏名",
                                "E_TEL"             => "急用電話番号",
                                "FINSCHOOL_NAME"    => $finschoolnametext
                              );

            $header[2] = array( "HR_NAME"           => "年組",
                                "STAFFNAME"         => "担任名",
                                "SCHREGNO"          => $schregnotext,
                                "ATTENDNO"          => $attendnotext,
                                "NAME"              => "氏名",
                                "NAME_KANA"         => "ふりがな",
                                "BIRTHDAY"          => "生年月日",
                                "SEX"               => "性別",
                                "ZIPCD"             => "郵便番号",
                                "ADDR1"             => "現住所",
                                "TEL"               => "電話番号",
                                "GUARD_NAME"        => "保護者氏名",
                                "E_TEL"             => "急用電話番号",
                                "FINSCHOOL_NAME"    => $finschoolnametext,
                                "REMARK"            => "備考"
                              );
        }

        $header[3]["HR_NAME"] = "年組";
        $header[3]["STAFFNAME"] = "担任名";
        $header[3]["ATTENDNO"] = "出席番号";
        $header[3]["NAME"] = "氏名";
        $header[3]["SEX"] = "性別";
        $header[3]["GUARD_NAME"] = "保護者氏名";
        $header[3]["ZIPCD"] = "郵便番号";
        $header[3]["ADDR1"] = "現住所";
        if ($this->z010Name1 == 'bunkyo') {
            $header[3]["ADDR2"] = "現住所２";
        }
        $header[3]["TEL"] = "電話番号";
        if ($this->field["FORM3_CLUB_CHECK"] == "1") {
            $header[3]["CLUBNAME"] = "部活動";
        }
        if ($this->field["FORM3_GRD_CHECK"] == "1") {
            $header[3]["FINSCHOOL_NAME"] = $finschoolnametext;
        }

        $header[4] = array( "HR_NAME"           => "年組",
                            "SCHREGNO"          => $schregnotext4,
                            "ATTENDNO"          => "出席番号",
                            "NAME"              => "氏名",
                            "NAME_KANA"         => $namekanatext4,
                            "SEX"               => "性別",
                            "BIRTHDAY"          => "生年月日",
                            "FINSCHOOL_NAME"    => $finschoolnametext,
                            "ENT_DIV_NAME"      => "入学種別"
                          );

        $header[5] = array( "ATTENDNO"          => "番号",
                            "NAME"              => "氏名",
                            "NAME_KANA"         => "ふりがな",
                            "SEX"               => "性別",
                            "BIRTHDAY"          => "生年月日",
                            "SCHREGNO"          => "学籍番号",
                            "FINSCHOOL_NAME"    => $finschoolnametext,
                            "ZIPCD"             => "郵便番号",
                            "ADDR1"             => "住所",
                            "TEL"               => "電話番号",
                            "E_TEL"             => "急用連絡先",
                            "GUARD_NAME"        => "保護者名"
                          );
        $contents  = "";

        //ヘッダをセット
        $contents .= implode($header[$this->field["FORM"]], ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //印刷対象項目
        $item = array("TEL", "E_TEL", "SEX", "BIRTHDAY");

        //データをセット
        $query = knja171Query::getCsvQuery($this);
        $result = $db->query($query);
        $data = false;
        $defSchregno = "";
        $wkStr = "";

        //職員情報取得
        $staffInfo = $db->getRow(knja171Query::getStaffInfo(), DB_FETCHMODE_ASSOC);
        $jpOrEngFlg = $staffInfo["FIELD5"] ? true : false;

        $keyGrade = "";
        $keyHrClass = "";
        $recCnt = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //ヘッダ項目のみセット
            $recCnt++;
            $nowGrdStr = "";
            $nowHrClsStr = "";
            $this->setKeyStr($row, $nowGrdStr, $nowHrClsStr);  //この関数内で場合分けして、キー項目や設定値を編集
            foreach ($header[$this->field["FORM"]] as $key => $val) {
                //英語表記時, ふりがなの値に英語をセット
                if ($key == "NAME_KANA" && $jpOrEngFlg) {
                    $row["NAME_KANA"] = $row["NAME_ENG"];
                }

                if ($this->field["FORM3_CLUB_CHECK"] == "1" && $defSchregno == $row["SCHREGNO"]) {
                    //同一生徒の場合、部活動のみを印字
                    if ($key == "CLUBNAME") {
                        $contents .= $row[$key].",";
                    } else {
                        $contents .= ",";
                    }
                } else {
                    if ($keyGrade != "" && $keyHrClass != "" && $keyGrade . $keyHrClass != $nowGrdStr . $nowHrClsStr) {
                        $recCnt = 1;
                    }
                    //出席番号変換(null、""以外を数値変換)
                    if ($key == "ATTENDNO" && !is_null($row[$key]) && $row[$key] !== "") {
                        if ($this->field["CHOICE"] == "1" && $this->Properties["useSpecial_Support_Hrclass"] == "1" && $this->field["GAKUNEN_KONGOU"] == "1") {
                            $row[$key] = $recCnt;
                        } else {
                            $row[$key] = intval($row[$key]);
                        }
                    }
                    //和暦変換
                    if ($key == "BIRTHDAY") {
                        $row[$key] = str_replace("-", "/", $row[$key]);
                    }
                    //印刷対象項目チェックなしのデータ
                    if (in_array($key, $item) && $this->field[$key] == "") {
                        $row[$key] = "";
                    }

                    $contents .= $row[$key].",";
                }
                $wkStr = $row["SCHREGNO"];
            }
            $defSchregno = $wkStr;
            $keyGrade = $nowGrdStr;
            $keyHrClass = $nowHrClsStr;
            $contents .= "\n";
            $data = true;
        }

        //DB切断
        Query::dbCheckIn($db);
        if ($data) {
            $couse = $this->sch_label."名簿";
            $filename = sprintf("%s.csv", $couse);
            common::downloadContents($contents, $filename, "SJIS-win");
            return true;
        } else {
            $this->setWarning("MSG303");
            return false;
        }
    }
    public function setKeyStr(&$row, &$grd, &$hrCls)
    {
        if ($this->isGhr) {
            $grd = $row["GHR_CD"];
            $hrCls = "1";
            $row["HR_NAME"] = $row["GHR_CLASSNAME"];
        } elseif ($this->isGakunenKongou) {
            $grd = $row["SCHOOL_KIND"];
            $hrCls = $row["GHR_CD"];
            $row["HR_NAME"] = $row["COURSENAME"] . " " . $row["GK_CLASSNAME"];
            $row["STAFFNAME"] = "";  //担任名を消す
        } else {
            $grd = $row["GRADE"];
            $hrCls = $row["HR_CLASS"];
        }
    }
}

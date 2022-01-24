<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjh132Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
    var $selectdata;
    var $z010Name1;
        
    /**
     * メンバ変数の初期化
     */
    function knjh132Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
    }
    function init()
    {               
        $this->field = array("YEAR"                 =>  VARS::post("YEAR"),                 //年度
                             "SEMESTER"             =>  VARS::post("SEMESTER"),             //学期コンボ
                             "GRADE_HR_CLASS"       =>  VARS::post("GRADE_HR_CLASS"),       //クラス
                             "TEL"                  =>  VARS::post("TEL"),                  //電話番号チェックボックス
                             "E_TEL"                =>  VARS::post("E_TEL"),                //急用電話番号チェックボックス
                             "SEX"                  =>  VARS::post("SEX"),                  //性別チェックボックス
                             "BIRTHDAY"             =>  VARS::post("BIRTHDAY"),             //生年月日チェックボックス
                             "FORM"                 =>  VARS::post("FORM"),                 //フォーム選択コンボ
                             "FORM3_CHECK"          =>  VARS::post("FORM3_CHECK"),          //部活動、出身校印字
                             "DBNAME"               =>  VARS::post("DBNAME")                //DB変更に備えて
                            );

        $this->cmd         = VARS::request("cmd");
        $this->selectdata = VARS::post("selectdata");
        $this->selectdataArea = VARS::post("selectdataArea");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //生徒項目名切替処理
        $schName = "";
        $db = Query::dbCheckOut();
        //テーブルの有無チェック
        $query = knjh132Query::checkTableExist();
        $table_cnt = $db->getOne($query);
        //生徒項目名取得
        $schName = $db->getOne(knjh132Query::getSchName($this));
        $this->sch_label = (strlen($schName) > 0) ? $schName : '生徒';
        $this->z010Name1 = $db->getOne(knjh132Query::getZ010Name1());
        Query::dbCheckIn($db);
    }

    //CSV出力処理
    function getCsvModel() {
        if ($this->z010Name1 == 'sundaikoufu') {
            $schregnotext = "学籍番号";
            $schregnotext4 = "学籍番号";
            $namekanatext4 = "読み";
            $attendnotext = "出席番号";
            $finschoolnametext = SCHOOLKIND == "P" ? "出身園" : "出身学校";
        } else {
            $schregnotext = $this->sch_label."手帳番号";
            $schregnotext4 = $this->sch_label."番号";
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
        if ($this->field["FORM3_CHECK"] == "1") {
            $header[3]["CLUBNAME"] = "部活動";
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

        $contents  = "";

        //ヘッダをセット
        $contents .= implode($header[$this->field["FORM"]], ",")."\n";

        //DB接続
        $db = Query::dbCheckOut();

        //印刷対象項目
        $item = array("TEL", "E_TEL", "SEX", "BIRTHDAY");

        //データをセット
        $query = knjh132Query::getCsvQuery($this);
        $result = $db->query($query);
        $data = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //ヘッダ項目のみセット
            foreach ($header[$this->field["FORM"]] as $key => $val) {
                //和暦変換
                if ($key == "BIRTHDAY") $row[$key] = str_replace("-","/",$row[$key]);
                //印刷対象項目チェックなしのデータ
                if (in_array($key, $item) && $this->field[$key] == "") $row[$key] = "";

                $contents .= $row[$key].",";
            }
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
}
?>

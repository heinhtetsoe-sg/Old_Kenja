<?php

require_once('for_php7.php');


// kanji=漢字

class knjg010Model extends Model
{
    public $field      = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $disp;        //primary
    public $year;        //年度
    public $semester;    //学期
    public $grade_hr_class;    //学年・組
    public $grade;       //学年
    public $hr_class;    //組
    public $attendno;    //出席番号
    public $issue_data;
    public $delete_data = array();
    public $disp2;        //
    public $school_name;    //学校名

    public function knjg010Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->year = $this->control["年度"];
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->staffcd);
            unset($this->field);
            unset($this->disp);
            unset($this->disp2);
        }

        $this->field = array(
                             "CERTIF_INDEX"        =>  VARS::post("CERTIF_INDEX"), //証明書連番
                             "SCHREGNO"            =>  VARS::request("SCHREGNO"),  //学籍番号
                             "CERTIF_KD"           =>  VARS::post("CERTIF_KD"),    //証明書種類コード
                             "SEMESTER"            =>  VARS::post("SEMESTER"),     //証明書交付学期
                             "GRADE"               =>  VARS::post("GRADE"),        //証明書交付学年
                             "CLASS"               =>  "00",                       //証明書交付組
                             "KISAI_SEKI"          =>  VARS::post("KISAI_SEKI"),   //記載責任者
                             "KJ_OUT"              =>  VARS::post("KJ_OUT"),       //漢字出力
                             "MIRISYU"             =>  VARS::post("MIRISYU"),      //未履修科目出力
                             "RISYU"               =>  VARS::post("RISYU"),        //履修のみ科目出力
                             "FORM6"               =>  VARS::post("FORM6"),        //６年用フォーム選択
                             "PR_DATE"             =>  VARS::post("PR_DATE"),      //処理年月日
                             "CERTIF_NO"           =>  VARS::post("CERTIF_NO"),    //証明書番号
                             "REMARK1"             =>  VARS::request("REMARK1"),   //入力発行番号
                             "DISP_CERTIF_NO"      =>  VARS::request("CERTIF_NO"), //入力発行番号
                             "TK_DATE"             =>  VARS::post("TK_DATE"),      //申請年月日
                             "BN_DATE"             =>  VARS::post("BN_DATE"),      //生年月日
                             "SOTUGYOU"            =>  VARS::post("SOTUGYOU"),     //卒業年月日
                             "NAME"                =>  VARS::post("NAME"),         //氏名
                             "HR_CLASS"            =>  VARS::post("HR_CLASS"),     //年組
                             "HR_TEARCH"           =>  VARS::post("HR_TEARCH"),    //学級担任
                             "CERTIF_KD"           =>  VARS::request("CERTIF_KD"), //証明書種類コード+各発行区分
                             "ISSUECD"             =>  "0",                        //証明書発行区分
                             "GRADUATE_FLG"        =>  VARS::post("GRADUATE_FLG"), //既卒区分
                             "KATEI"               =>  VARS::post("KATEI"),        //課程
                             "GAKKA"               =>  VARS::post("GAKKA"),        //学科
                             "HYOUTEI"             =>  VARS::post("HYOUTEI"),      //評定
                             "SONOTAJUUSYO"        =>  VARS::post("SONOTAJUUSYO"), //その他住所を優先
                             "GAIHYOU"             =>  VARS::post("GAIHYOU"),      //概評
                             "tyousasyoNotPrintAnotherAttendrec"      =>  VARS::post("tyousasyoNotPrintAnotherAttendrec"),      //出欠の前籍校を含まない
                             "KISAI_FLG"           =>  VARS::post("KISAI_FLG"),    //記載責任者(変更あり)
                             "ENT_GRD_DATE_FORMAT" =>  VARS::post("ENT_GRD_DATE_FORMAT"),    //入学・卒業日付は年月で表示する
                             "PRINT_STAMP"         =>  VARS::post("PRINT_STAMP"),  //印影出力する
                             "HANKI_NINTEI_FORM"   =>  VARS::post("HANKI_NINTEI_FORM"),   // 半期認定フォームで出力
                             "RYUGAKU_CREDIT"      =>  VARS::post("RYUGAKU_CREDIT"), //留学の単位数を0表示
                             "SOGAKU_CREDIT"       =>  VARS::post("SOGAKU_CREDIT"),  //総合的な学習の時間の単位数を0表示
                             "KNJE070_CHECK_PRINT_STAMP_PRINCIPAL" => VARS::post("KNJE070_CHECK_PRINT_STAMP_PRINCIPAL"), // 校長印（調査書のみ）
                             "KNJE070_CHECK_PRINT_STAMP_HR_STAFF"  => VARS::post("KNJE070_CHECK_PRINT_STAMP_HR_STAFF"),   // 担任印（調査書のみ）
                             "KNJE070D_PRINTHEADERNAME" => VARS::post("KNJE070D_PRINTHEADERNAME"),   // 偶数頁に氏名を出力する（調査書のみ）
                             "GVAL_CALC_CHECK"     => VARS::post("GVAL_CALC_CHECK"),   // 評定平均算出
                             "PRINT_AVG_RANK"      => VARS::post("PRINT_AVG_RANK"),   // 席次出力
                             "tyousasyo2020shojikouExtendsSelect" => VARS::post("tyousasyo2020shojikouExtendsSelect")   // 出力ページ選択
                            );

        $this->cmd          = VARS::request("cmd");
        $this->issue_data   = VARS::request("category_name");
        $this->delete_data  = VARS::request("category_selected");

        $this->disp         = VARS::request("DISP") ? VARS::request("DISP") : $this->disp;
        $this->disp2        = VARS::request("DISP2") ? VARS::request("DISP2") : $this->disp;

        $db = Query::dbCheckOut();
        $query = knjg010Query::getSchoolName();
        $this->school_name = $db->getOne($query);
        Query::dbCheckIn($db);

        if (isset($this->field["HR_CLASS"])) {
            $this->field["GRADE"]        = substr($this->field["HR_CLASS"], 0, 1);
            $this->field["CLASS"]        = substr($this->field["HR_CLASS"], 1, 2);
        }
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //初期値の設定
        $this->Properties["useSyojikou3"]                      = ($this->Properties["useSyojikou3"]                      == '1') ? $this->Properties["useSyojikou3"]                      : 0;
        $this->Properties["certifNoSyudou"]                    = ($this->Properties["certifNoSyudou"]                    == '1') ? $this->Properties["certifNoSyudou"]                    : 0;
        $this->Properties["Knje080UseAForm"]                   = ($this->Properties["Knje080UseAForm"]                   == '1') ? $this->Properties["Knje080UseAForm"]                   : 0;
        $this->Properties["tyousasyoAttendrecRemarkFieldSize"] = ($this->Properties["tyousasyoAttendrecRemarkFieldSize"] != '')  ? $this->Properties["tyousasyoAttendrecRemarkFieldSize"] : '';
        $this->Properties["tyousasyoTaniPrint"]                = ($this->Properties["tyousasyoTaniPrint"] == '2')                ? $this->Properties["tyousasyoTaniPrint"]                : 1;
        $this->Properties["tyousasyoKinsokuForm"]              = ($this->Properties["tyousasyoKinsokuForm"])                     ? $this->Properties["tyousasyoKinsokuForm"]              : "";
    }

    public function &getIssueModel()
    {
        //発行処理
        for ($i = 0; $i < get_count($this->issue_data); $i++) {
            $work = explode(",", $this->issue_data[$i]);

            $get_certif_maxnumchk = knjg010Query::getCertifMaxnumchk($this->year, $work[21]); //２度押しチェック
            if ($get_certif_maxnumchk == 0) {
                $certif_no_div = knjg010Query::getCertifSchool($this->year, $work[1]);
                if ($certif_no_div == 0) {
                    $get_certif_maxnumchk = knjg010Query::getCertifMaxnumber($this->year, $this, $work[21]);
                }
                $result = knjg010Query::getUpdateQuery(
                    $this->year,
                    $work[21],
                    knjg010Query::getissuername(STAFFCD),
                    $get_certif_maxnumchk,
                    $this->control["学籍処理日"],
                    $certif_no_div,
                    $this
                );

                $this->insertIssuePrintHist($work);
            }

            //発行番号の値はプロパティーファイルでそのままにするか、DBの値を使う。
            if ($this->Properties["certifNoSyudou"] != '1') {
                $work[9] = $get_certif_maxnumchk; // -> インデクスは11(REMARK1)ではなく9(CERTIF_NO)
            }

            $tmp = "";
            for ($j = 0; $j < 41; $j++) {
                $tmp .= $work[$j] .",";
            }
            $tmp .= $work[$j];
            $this->issue_data[$i] = $tmp;
        }
        //$this->setMessage(MC0005);
    }

    public function &deleteIssueModel()
    {
        //削除処理
        for ($i = 0; $i < get_count($this->delete_data); $i++) {
            $work = explode(",", $this->delete_data[$i]);

            $result = knjg010Query::getDeleteQuery($this->year, $work[21]);
        }
        //$this->setMessage(MC3005);
    }

    public function schregChk($schregno)
    {
        $row = knjg010Query::getSchregregdData($this);
        if (!isset($row)) {
            $this->setWarning("", "該当する学籍番号はありません。");
            return false;
        }
        return true;
    }

    //入力項目チェック
    public function validateInput()
    {
        //存在チェック
        if ($this->cmd == "update") {
            $row1 = knjg010Query::getCertifNumber($this->year, $this->field["CERTIF_NO"]);
            if (is_array($row1)) {
                $this->setWarning("MZ0024");
                return false;
            }
        }
        return true;
    }


    public function &getInsertModel()
    {
        $Row3 = knjg010Query::getSchregTransferData($this);

        $work = explode(",", $this->field["CERTIF_KD"]);

        if (!isset($Row3["TRANSFERCD"])) {
            if ($work[2] != '1') {
                $this->setWarning("", "証明書の発行対象者ではありません。");
                return;
            }
                $this->field["GRADUATE_FLG"] = 0;
        } elseif (isset($Row3["TRANSFERCD"]) && ($Row3["TRANSFERCD"] == '2' || $Row3["TRANSFERCD"] == '3')) {
            if ($work[4] != '1') {
                $this->setWarning("", "証明書の発行対象者ではありません。");
                return;
            }
                $this->field["GRADUATE_FLG"] = 0;
        } elseif (isset($Row3["TRANSFERCD"]) && $Row3["TRANSFERCD"] == '1') {
            if ($work[3] != '1') {
                $this->setWarning("", "証明書の発行対象者ではありません。");
                return;
            }
                $this->field["GRADUATE_FLG"] = 1;
        }

        $message = "";
        //更新の時のチェック
        if ($this->cmd === 'update') {
            $this->field["GET_SCHREGNO"] = VARS::request("GET_SCHREGNO");
            $this->field["CERTIF_INDEX"] = VARS::request("GET_CERTIF_INDEX");
            if ($this->field["GET_SCHREGNO"] != $this->field["SCHREGNO"]) {
                $this->setWarning("MSG308", "更新時は学籍番号の変更はできません。");
                return false;
            }
            //発行番号の重複チェック（発行番号手動登録のとき）
            if ($this->Properties["certifNoSyudou"] == "1" && $this->field["REMARK1"] != "") {
                if ($this->Properties["certifNoSyudouCheck"]) {
                    $No_check = knjg010Query::getCertifNoSyudouCheck($this, "");
                    $get_certif_index = knjg010Query::getCertifNoSyudouCheck($this, "index");

                    if ($No_check > 0 && $this->field["CERTIF_INDEX"] != $get_certif_index) {
                        $this->setWarning("MSG302", "　　（発行番号）");
                        return;
                    }
                }
            }
        } else {
            //追加の時のチェック
            //発行番号の重複チェック（発行番号手動登録のとき）
            if ($this->Properties["certifNoSyudou"] == "1" && $this->field["REMARK1"] != "") {
                if ($this->Properties["certifNoSyudouCheck"]) {
                    $No_check = knjg010Query::getCertifNoSyudouCheck($this, "");

                    if ($No_check > 0) {
                        $this->setWarning("MSG302", "　　（発行番号）");
                        return;
                    }
                }
            }
            // 留学・休学の生徒の卒業見込証明書は卒業予定年月確認メッセージを表示する
            $work = explode(",", $this->field["CERTIF_KD"]);
            $certifKindcd = $work[0];
            if ($certifKindcd == '003') { // 卒業見込証明書
                if (knjg010Query::hasOffdaysOrAbroad($this->field["SCHREGNO"])) {
                    $message = "\\n休学、留学期間があるので\\n卒業予定年月を確認して下さい。";
                }
            }
            $row1 = knjg010Query::getCertifIndexMax($this->year);
            $this->field["CERTIF_INDEX"] = $row1["INDEX"];
        }
        $this->field["CERTIF_NO"] = "0";
        $this->insert();
        $this->setMessage("MSG201", $message);
    }

    public function &getCancelModel()
    {

        $this->field["CERTIF_INDEX"] = VARS::request("GET_CERTIF_INDEX");
        knjg010Query::setCancelQuery($this);
        $this->setMessage("MSG201");
    }

    public function &insert()
    {
        $result = knjg010Query::setInsertQuery($this, $this->field, $this->year, $this->cmd);
    }


    public function clean()
    {
    }

    public function &insertIssuePrintHist($work)
    {

        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjg010Query::getMaxPrintNo(CTRL_YEAR, $work[21]);
        $maxPrintNo = $db->getOne($query);
        $query = knjg010Query::insertPrintHist(CTRL_YEAR, $work[21], $maxPrintNo);
        $db->query($query);

        $db->commit();
        //DB切断
        Query::dbCheckIn($db);
    }
}

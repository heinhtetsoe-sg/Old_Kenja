<?php

require_once('for_php7.php');
/**
 * 定数定義
*/

class knje061Model extends Model
{
    public $field      = array();
    public $data       = array();
    public $cmd;
    public $file;
    public $annual;            //年次
    public $control;           //コントロールマスタ
    public $hr_class;
    public $coursecode;
    public $RegdTable;         //学校区分別のテーブル名
    public $schoolName;
    public $recordTableDiv;
//    var $maxSemester;
    public $vSchregRegdDat;    //在籍データ(生徒のMAX学期のレコード)
    public $check_kanten;
    public $annualSchoolKind;  // 年次の学校種別
    public $lastColumn;//CSVの最後の列の値

    /**
     * メンバ変数の初期化
    */
    public function knje061Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        if ($this->control["学校区分"] == "0") { //学年制
            $this->RegdTable = "v_regdyear_grade_dat";
        } elseif ($this->control["学校区分"] == "1") { //単位制
            $this->RegdTable = "v_regdyear_unit_dat";
        }
//        $this->maxSemester = knje061Query::getMaxSemester();
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            $this->vSchregRegdDat = knje061Query::getViewSchregRegdDat();
        }
        $this->cmd    = VARS::request("cmd");
        //年次
        $this->annual = VARS::post("ANNUAL");
        //組
        $this->hr_class = VARS::post("HR_CLASS");
        //コース
        $this->coursecode = VARS::post("COURSECODE");
        $this->schregno   = VARS::post("SCHREGNO");
        //集計月
        $this->target_month = VARS::post("TARGET_MONTH");

        //年次が変更したら初期化する
        if ($this->cmd == "annual") {
            $this->hr_class = "";
            $this->coursecode = "";
        }
        //組を変更したら初期化する
        if ($this->cmd == "hr_class") {
            $this->coursecode = "";
        }

        if (VARS::file("FILE")) {
            $this->file = VARS::file("FILE");        //ファイルのアップロード
        }
        //画面上のデータ
        $this->field = array("KIND"     => VARS::post("KIND"),         //種別
                            "RANGE"     => 1,        //生成範囲
                            "METHOD"    => VARS::post("METHOD"),       //生成方法
                            "CREATEDIV" => VARS::post("CREATEDIV"),     //新規作成 OR 追加作成
                            "HEADERFLG" => VARS::post("HEADERFLG"));   //ヘッダ有りフラグ
        //成績データ　の下の観点データ作成（中学）チェックボックス
        $this->check_kanten = VARS::post("CHECK_KANTEN");
        // 成績テーブルを判断するためのフラグを取得(法政・自修館・その他)
        $rtnRow = knje061Query::getNameMst();
        $this->schoolName       = $rtnRow["NAME1"];
        $this->recordTableDiv   = $rtnRow["NAMESPARE1"];
        // 観点の計算方法
        $rtnRow2 = knje061Query::getNameMstD027();
        $this->keisanDiv = $rtnRow2["NAMESPARE1"];

        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

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
        $header = array("SCHOOLCD"          => "本校区分",
                        "YEAR"              => "年度",
                        "SCHREGNO"          => "学籍番号",
                        "ANNUAL"            => "年次",
                        "CLASSCD"           => "教科コード",
                        "SCHOOL_KIND"       => "学校校種",
                        "CURRICULUM_CD"     => "教育課程コード",
                        "SUBCLASSCD"        => "科目コード",
                        "VIEWCD"            => "観点コード");
        //実行履歴
        //knje061Query::executeRireki($this);
        if ($this->field["KIND"] == 2) {
            if ($this->field["METHOD"] == 2) { //csv取込み
                //ファイル取り込み
                $this->dataArr = array();
                $this->errData = array();
                $this->dataArr = $this->fileUpload();
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                //データの整合性チェック
                $erraDatas = "";
                $tables = array();
                $tables = array("1" => "SCHREG_STUDYREC_DAT", "2" => "STUDYRECREMARK_DAT", "3" => "STUDYREC_PROV_FLG_DAT");
                $dupTable = $this->getDuplicateErrorHeaderNoSave(PROGRAMID, $tables, $this->errData);
                if (sizeof($dupTable) > 0) {
                    $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                }
                if (sizeof($this->errData) > 0) {
                    $erraDatas .= "行番号　メッセージ"."\\n";
                }
                foreach ($this->errData as $errKey => $errVal) {
                    $erraDatas .= $errKey."行目　".$errVal["MSG"]."\\n";
                }
                if ($erraDatas != "") {
                    $this->setMessage("MSG204", "\\n"."取込みエラー内容:". "\\n".$headerErr["MSGREMARK"]. "\\n".$erraDatas);
                } else {
                    for ($i = 0; $i < get_count($this->dataArr); $i++) {
                        $db->expectError();
                        //学籍学習記録データの削除
                        $result = $db->query(knje061Query::deleteQuerySchregStudyRecDat($this->dataArr[$i], $this));
                        if (PEAR::isError($result)) {
                            continue;
                        }
                        //仮評定フラグ
                        if ($this->Properties["useProvFlg"] == '1') {
                            //仮評定欄が、nullの時は、STUDYREC_PROV_FLG_DATのレコードを削除
                            //仮評定欄が、1の時は、STUDYREC_PROV_FLG_DATのレコードを作成
                            if (!strlen($this->dataArr[$i]["PROV_FLG"])) {
                                $result = $db->query(knje061Query::deleteStudyRecProvFlgCsv($this->dataArr[$i], $this));
                                if (PEAR::isError($result)) {
                                    continue;
                                }
                            } else {
                                $result = $db->query(knje061Query::deleteStudyRecProvFlgCsv($this->dataArr[$i], $this));
                                if (PEAR::isError($result)) {
                                    continue;
                                }
                                $result = $db->query(knje061Query::insertStudyRecProvFlgCsv($this->dataArr[$i], $this));
                                if (PEAR::isError($result)) {
                                    continue;
                                }
                            }
                        }
                        //CLASS_MSTより科目数を取得
                        $subclasses = $db->getOne(knje061Query::getSubclasses($this->dataArr[$i], $this));
                        $this->dataArr[$i]["SUBCLASSES"] = $subclasses;

                        //学籍学習記録データの作成（ＣＳＶデータより読込)
                        $result = $db->query(knje061Query::addQuerySchregStudyRecDat($this->dataArr[$i], $this));
                        if (PEAR::isError($result)) {
                            continue;
                        }
                        $result = $db->query(knje061Query::deleteStudyRecRemarkCsv($this->dataArr[$i], $this));
                        if (PEAR::isError($result)) {
                            continue;
                        }
                        $result = $db->query(knje061Query::insertStudyRecRemarkCsv($this->dataArr[$i], $this));
                        if (PEAR::isError($result)) {
                            continue;
                        }
                        $db->popExpect();
                    }
                    $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
                }
                $db->commit();
                Query::dbCheckIn($db);
            } else {
                if ($this->field["CREATEDIV"] == 1) {
                    //新規作成
                    knje061Query::addRecordSchregStudyrecDat($this);
                } elseif ($this->field["CREATEDIV"] == 2) {
                    //追加作成
                    knje061Query::addRecordSchregStudyrecDat2($this);
                } else {
                    //上書作成
                    knje061Query::addRecordSchregStudyrecDat3($this);
                }
                // 広島国際の場合のみ「総合学習(900100)とＬＨＲ(940000)」
                if ($this->schoolName == "hirogaku") {
                    knje061Query::hirogakuStudyRec($this);
                }
                //どの資格データへ取込むか(1:STUDYRECREMARK_QUALIFIED_DAT それ以外:STUDYRECREMARK_DAT)
                if ($this->Properties["useStudyrecRemarkQualifiedDat"] == 1) {
                    knje061Query::kumamotoStudyrecRemarkQualified($this);
                }
                //仮評定フラグのコピー(宮城県・常磐)
                if ($this->Properties["useProvFlg"] == '1') {
                    knje061Query::tokiwaStudyRecProvFlg($this);
                }
                //学習記録データの生成(大宮)・・・先科目のみ
                if ($this->Properties["useFromStudyrecToJviewstat"] == 1) {
                    knje061Query::oomiyaStudyRecSaki($this);
                }
                $this->message = "学籍学習記録データの生成が完了しました。";
                //観点データの生成(大宮)
                if ($this->Properties["useFromStudyrecToJviewstat"] == 1 && $this->check_kanten == 'on') {
                    $this->updateKantenOomiya();
                    $this->message = "学籍学習記録データおよび観点データの生成が完了しました。";
                }
            }
        } elseif ($this->field["KIND"] == 1) { //"勤怠データ抽出＆集計中..."
            if ($this->field["METHOD"] == 2) { //csv取込み
                //ファイル取り込み
                $this->dataArr = array();
                $this->errData = array();
                $this->dataArr = $this->fileUpload();
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                $erraDatas = "";
                $tables = array();
                $tables = array("1" => "SCHREG_ATTENDREC_DAT");
                $dupTable = $this->getDuplicateErrorHeaderNoSave(PROGRAMID, $tables, $this->errData);
                if (sizeof($dupTable) > 0) {
                    $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                }
                if (sizeof($this->errData) > 0) {
                    $erraDatas .= "行番号　メッセージ"."\\n";
                }
                foreach ($this->errData as $errKey => $errVal) {
                    $erraDatas .= $errKey."行目　".$errVal["MSG"]."\\n";
                }
                if ($erraDatas != "") {
                    $this->setMessage("MSG204", "\\n"."取込みエラー内容:". "\\n".$headerErr["MSGREMARK"]. "\\n".$erraDatas);
                } else {
                    for ($i = 0; $i < get_count($this->dataArr); $i++) {
                        $db->expectError();
                        //学籍学習記録データの削除
                        $result = $db->query(knje061Query::deleteQuerySchregAttendRecDat($this->dataArr[$i]));
                        if (PEAR::isError($result)) {
                            continue;
                        }
                        //学籍学習記録データの作成（ＣＳＶデータより読込)
                        $result = $db->query(knje061Query::addQuerySchregAttendRecDat($this->dataArr[$i]));
                        if (PEAR::isError($result)) {
                            continue;
                        }
                        $db->popExpect();
                    }
                    $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
                }
                $db->commit();
                Query::dbCheckIn($db);
            } else {
                //出欠学期別累積データより集計
                knje061Query::addRecordSchregAttendrecDat($this);
                $this->message = "学籍出欠記録データの生成が完了しました。";
            }
        } elseif ($this->field["KIND"] == 3) {
            if ($this->field["METHOD"] == 2) { //csv取込み
                //ファイル取り込み
                $this->dataArr = array();
                $this->errData = array();
                $this->dataArr = $this->fileUpload();
                $db = Query::dbCheckOut();
                $db->autoCommit(false);
                $erraDatas = "";
                $tables = array();
                $tables = array("1" => "JVIEWSTAT_SUB_DAT");
                $dupTable = $this->getDuplicateErrorHeaderNoSave(PROGRAMID, $tables, $this->errData);
                if (sizeof($dupTable) > 0) {
                    $headerErr = $this->duplicateCsvHeader($dupTable, $header);
                }
                if (sizeof($this->errData) > 0) {
                    $erraDatas .= "行番号　メッセージ"."\\n";
                }
                foreach ($this->errData as $errKey => $errVal) {
                    $erraDatas .= $errKey."行目　".$errVal["MSG"]."\\n";
                }
                if ($erraDatas != "") {
                    $this->setMessage("MSG204", "\\n"."取込みエラー内容:". "\\n".$headerErr["MSGREMARK"]. "\\n".$erraDatas);
                } else {
                    for ($i = 0; $i < get_count($this->dataArr); $i++) {
                        $db->expectError();
                        //削除
                        $result = $db->query(knje061Query::delCsvDataJview($this->dataArr[$i], $this));
                        if (PEAR::isError($result)) {
                            continue;
                        }
                        //作成（ＣＳＶデータより読込)
                        $result = $db->query(knje061Query::insCsvDataJview($this->dataArr[$i], $this));
                        if (PEAR::isError($result)) {
                            continue;
                        }
                        $db->popExpect();
                    }
                    $this->setMessage("MSG202", "更新件数".get_count($this->dataArr)."件");
                }
                $db->commit();
                Query::dbCheckIn($db);
            } else {
                if ($this->Properties["useKantenGakunenSeiseki"] == '1') {
                    //成績データの学年成績から観点データを生成
                    $this->updateKantenKaijou();
                } elseif ($this->Properties["useKantenGakunenSeiseki"] == '2' || $this->Properties["useKantenGakunenSeiseki"] == '3') {
                    //観点データの生成(玉川聖)(成城)
                    //(玉川聖)成績データ(9-990008)から観点データを生成（観点変換テーブル参照）
                    //(成城)  成績データ(9-990009)から観点データを生成（観点変換テーブル参照）(観点はランダムに割振る)
                    $this->updateKantenTamagawa();
                } else {
                    //JVIEWSTAT_RECORD_DATから観点データを生成
                    $this->updateKanten();
                }
                $this->message = "観点データの生成が完了しました。";
            }
        } elseif ($this->field["KIND"] == 4) {
            if ($this->field["METHOD"] == 2) {
                //ファイル取り込み
                //$this->fileUpload();
            } else {
                //行動の記録他
                $this->updateBehavior();
                $this->message = "行動の記録他データの生成が完了しました。";
            }
        }
        //実行履歴
        knje061Query::executeRireki($this);
    }

    public function fileUpload()
    {
        $tempArr = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        if (!is_uploaded_file($this->file["tmp_name"])) {
            $this->warning = "ファイル名が不正です。";
        } else {
            $fp = fopen($this->file["tmp_name"], "r");
            $checkdata = array();
            //学籍学習記録データのサイズ
            if ($this->field["KIND"] == 1) { //出欠データ
                $checkdata = array( "SCHOOLCD"          => array(1,"本校区分"),
                                    "GRADE"             => array("学年"),
                                    "HR_CLASS"          => array("組"),
                                    "ATTENDNO"          => array("出席番号"),
                                    "SCHREGNO"          => array(8,"学籍番号"),
                                    "NAME"              => array("生徒氏名"),
                                    "YEAR"              => array(4,"年度"),
                                    "ANNUAL"            => array(2,"履修学年"),
                                    "SUMDATE"           => array(10,"集計日付"),
                                    "CLASSDAYS"         => array(3,"授業日数"),
                                    "OFFDAYS"           => array(3,"休学日数"),
                                    "ABSENT"            => array(3,"公欠日数"),
                                    "SUSPEND"           => array(3,"出停日数"),
                                    "MOURNING"          => array(3,"忌引日数"),
                                    "ABROAD"            => array(3,"留学日数"),
                                    "REQUIREPRESENT"    => array(3,"要出席日数"),
                                    "SICK"              => array(3,"病欠日数"),
                                    "ACCIDENTNOTICE"    => array(3,"事故欠届日数"),
                                    "NOACCIDENTNOTICE"  => array(3,"事故欠無日数"),
                                    "PRESENT"           => array(3,"出席日数"));
            } elseif ($this->field["KIND"] == 2) { //成績データ
                //教育課程対応
                if ($this->Properties["useCurriculumcd"] == '1') {
                    //仮評定フラグ
                    if ($this->Properties["useProvFlg"] == '1') {
                        $checkdata = array( "SCHOOLCD"          => array(1,"本校区分"),
                                            "GRADE"             => array("学年"),
                                            "HR_CLASS"          => array("組"),
                                            "ATTENDNO"          => array("出席番号"),
                                            "SCHREGNO"          => array(8,"学籍番号"),
                                            "NAME"              => array("生徒氏名"),
                                            "SEX"               => array("性別区分"),
                                            "SEX_NAME"          => array("性別"),
                                            "YEAR"              => array(4,"年度"),
                                            "ANNUAL"            => array(2,"履修学年"),
                                            "COURSECD"          => array("課程コード"),
                                            "MAJORCD"           => array("学科コード"),
                                            "COURSECODE"        => array("コースコード"),
                                            "COURSECODENAME"    => array("コースコード名"),
                                            "CLASSCD"           => array(2, "教科コード"),
                                            "CLASSNAME"         => array(30,"教科名"),
                                            "CLASSABBV"         => array(15,"教科略称"),
                                            "CLASSNAME_ENG"     => array(40,"教科名英字"),
                                            "CLASSABBV_ENG"     => array(30,"教科略称英字"),
                                            "SCHOOL_KIND"       => array(1,"学校種別"),
                                            "CURRICULUM_CD"     => array(1,"教育課程コード"),
                                            "SUBCLASSCD"        => array(6,"科目コード"),
                                            "SUBCLASSNAME"      => array(60,"科目名"),
                                            "SUBCLASSABBV"      => array(9,"科目略称"),
                                            "SUBCLASSNAME_ENG"  => array(40,"科目名英字"),
                                            "SUBCLASSABBV_ENG"  => array(20,"科目略称英字"),
                                            "PROV_FLG"          => array(1,"仮評定フラグ"),
                                            "VALUATION"         => array(2,"評価"),
                                            "GET_CREDIT"        => array(2,"標準修得単位"),
                                            "ADD_CREDIT"        => array(2,"増加修得単位"),
                                            "COMP_CREDIT"       => array(2,"履修単位"),
                                            "REMARK"            => array(60,"履修科目備考"));
                    } else {
                        $checkdata = array( "SCHOOLCD"          => array(1,"本校区分"),
                                            "GRADE"             => array("学年"),
                                            "HR_CLASS"          => array("組"),
                                            "ATTENDNO"          => array("出席番号"),
                                            "SCHREGNO"          => array(8,"学籍番号"),
                                            "NAME"              => array("生徒氏名"),
                                            "SEX"               => array("性別区分"),
                                            "SEX_NAME"          => array("性別"),
                                            "YEAR"              => array(4,"年度"),
                                            "ANNUAL"            => array(2,"履修学年"),
                                            "COURSECD"          => array("課程コード"),
                                            "MAJORCD"           => array("学科コード"),
                                            "COURSECODE"        => array("コースコード"),
                                            "COURSECODENAME"    => array("コースコード名"),
                                            "CLASSCD"           => array(2, "教科コード"),
                                            "CLASSNAME"         => array(30,"教科名"),
                                            "CLASSABBV"         => array(15,"教科略称"),
                                            "CLASSNAME_ENG"     => array(40,"教科名英字"),
                                            "CLASSABBV_ENG"     => array(30,"教科略称英字"),
                                            "SCHOOL_KIND"       => array(1,"学校種別"),
                                            "CURRICULUM_CD"     => array(1,"教育課程コード"),
                                            "SUBCLASSCD"        => array(6,"科目コード"),
                                            "SUBCLASSNAME"      => array(60,"科目名"),
                                            "SUBCLASSABBV"      => array(9,"科目略称"),
                                            "SUBCLASSNAME_ENG"  => array(40,"科目名英字"),
                                            "SUBCLASSABBV_ENG"  => array(20,"科目略称英字"),
                                            "VALUATION"         => array(2,"評価"),
                                            "GET_CREDIT"        => array(2,"標準修得単位"),
                                            "ADD_CREDIT"        => array(2,"増加修得単位"),
                                            "COMP_CREDIT"       => array(2,"履修単位"),
                                            "REMARK"            => array(60,"履修科目備考"));
                    }
                } else {
                    //仮評定フラグ
                    if ($this->Properties["useProvFlg"] == '1') {
                        $checkdata = array( "SCHOOLCD"          => array(1,"本校区分"),
                                            "GRADE"             => array("学年"),
                                            "HR_CLASS"          => array("組"),
                                            "ATTENDNO"          => array("出席番号"),
                                            "SCHREGNO"          => array(8, "学籍番号"),
                                            "NAME"              => array("生徒氏名"),
                                            "SEX"               => array("性別区分"),
                                            "SEX_NAME"          => array("性別"),
                                            "YEAR"              => array(4,"年度"),
                                            "ANNUAL"            => array(2,"履修学年"),
                                            "COURSECD"          => array("課程コード"),
                                            "MAJORCD"           => array("学科コード"),
                                            "COURSECODE"        => array("コースコード"),
                                            "COURSECODENAME"    => array("コースコード名"),
                                            "CLASSCD"           => array(2, "教科コード"),
                                            "CLASSNAME"         => array(30,"教科名"),
                                            "CLASSABBV"         => array(15,"教科略称"),
                                            "CLASSNAME_ENG"     => array(40,"教科名英字"),
                                            "CLASSABBV_ENG"     => array(30,"教科略称英字"),
                                            "SUBCLASSCD"        => array(6,"科目コード"),
                                            "SUBCLASSNAME"      => array(60,"科目名"),
                                            "SUBCLASSABBV"      => array(9,"科目略称"),
                                            "SUBCLASSNAME_ENG"  => array(40,"科目名英字"),
                                            "SUBCLASSABBV_ENG"  => array(20,"科目略称英字"),
                                            "PROV_FLG"          => array(1,"仮評定フラグ"),
                                            "VALUATION"         => array(2,"評価"),
                                            "GET_CREDIT"        => array(2,"標準修得単位"),
                                            "ADD_CREDIT"        => array(2,"増加修得単位"),
                                            "COMP_CREDIT"       => array(2,"履修単位"),
                                            "REMARK"            => array(60,"履修科目備考"));
                    } else {
                        $checkdata = array( "SCHOOLCD"          => array(1,"本校区分"),
                                            "GRADE"             => array("学年"),
                                            "HR_CLASS"          => array("組"),
                                            "ATTENDNO"          => array("出席番号"),
                                            "SCHREGNO"          => array(8,"学籍番号"),
                                            "NAME"              => array("生徒氏名"),
                                            "SEX"               => array("性別区分"),
                                            "SEX_NAME"          => array("性別"),
                                            "YEAR"              => array(4,"年度"),
                                            "ANNUAL"            => array(2,"履修学年"),
                                            "COURSECD"          => array("課程コード"),
                                            "MAJORCD"           => array("学科コード"),
                                            "COURSECODE"        => array("コースコード"),
                                            "COURSECODENAME"    => array("コースコード名"),
                                            "CLASSCD"           => array(2, "教科コード"),
                                            "CLASSNAME"         => array(30,"教科名"),
                                            "CLASSABBV"         => array(15,"教科略称"),
                                            "CLASSNAME_ENG"     => array(40,"教科名英字"),
                                            "CLASSABBV_ENG"     => array(30,"教科略称英字"),
                                            "SUBCLASSCD"        => array(6,"科目コード"),
                                            "SUBCLASSNAME"      => array(60,"科目名"),
                                            "SUBCLASSABBV"      => array(9,"科目略称"),
                                            "SUBCLASSNAME_ENG"  => array(40,"科目名英字"),
                                            "SUBCLASSABBV_ENG"  => array(20,"科目略称英字"),
                                            "VALUATION"         => array(2,"評価"),
                                            "GET_CREDIT"        => array(2,"標準修得単位"),
                                            "ADD_CREDIT"        => array(2,"増加修得単位"),
                                            "COMP_CREDIT"       => array(2,"履修単位"),
                                            "REMARK"            => array(60,"履修科目備考"));
                    }
                }
            } else { //観点データ
                //教育課程対応
                if ($this->Properties["useCurriculumcd"] == '1') {
                    $checkdata = array( "SCHREGNO"         => array(8,"学籍番号"),
                                        "YEAR"             => array(4,"年度"),
                                        "CLASSCD"          => array(2,"教科コード"),
                                        "SCHOOL_KIND"      => array(1,"学校種別"),
                                        "CURRICULUM_CD"    => array(1,"教育課程コード"),
                                        "SUBCLASSCD"       => array(6,"科目コード"),
                                        "VIEWCD"           => array(4,"観点コード"),
                                        "STATUS"           => array(1,"観点"));
                } else {
                    $checkdata = array( "SCHREGNO"         => array(8,"学籍番号"),
                                        "YEAR"             => array(4,"年度"),
                                        "SUBCLASSCD"       => array(6,"科目コード"),
                                        "VIEWCD"           => array(4,"観点コード"),
                                        "STATUS"           => array(1,"観点"));
                }
            }

            $i = $cntShomeiZumi = 0;
            $line = 1;
            $header = false;

            //取得主キー
            $primaryKey1    = array();
            $duplicateCsv1  = array();
            $primaryKey2    = array();
            $duplicateCsv2  = array();
            $primaryKey3    = array();
            $duplicateCsv3  = array();
            if ($this->field["KIND"] == 1) {
                $primaryKey1 = $this->getPrimaryKey('SCHREG_ATTENDREC_DAT');
            } elseif ($this->field["KIND"] == 2) {
                if ($this->Properties["useProvFlg"] == '1') {
                    $primaryKey3 = $this->getPrimaryKey('STUDYREC_PROV_FLG_DAT');
                }
                $primaryKey1 = $this->getPrimaryKey('SCHREG_STUDYREC_DAT');
                $primaryKey2 = $this->getPrimaryKey('STUDYRECREMARK_DAT');
            } else {
                $primaryKey1 = $this->getPrimaryKey('JVIEWSTAT_SUB_DAT');
            }

            while (!feof($fp)) {
                $buffer = fgets($fp, 4096);

                if (trim($buffer) != '') {
                    //ヘッダ有りの場合は1行目を無視
                    if ($this->field["HEADERFLG"] == "1" && !$header) {
                        $header = true;
                        $line++;
                        continue;
                    }

                    $buffer = trim(i18n_convert($buffer, i18n_http_output(), "SJIS-win"));
                    $tmp = common::csv2array($buffer);
                    $data = array();
                    if ($this->field["KIND"] == 1) { //出欠データ
                        $data = array(  "SCHOOLCD"         => $tmp[0],
                                        "YEAR"             => $tmp[6],
                                        "SCHREGNO"         => $tmp[4],
                                        "ANNUAL"           => $tmp[7],
                                        "SUMDATE"          => $tmp[8],
                                        "CLASSDAYS"        => $tmp[9],
                                        "OFFDAYS"          => $tmp[10],
                                        "ABSENT"           => $tmp[11],
                                        "SUSPEND"          => $tmp[12],
                                        "MOURNING"         => $tmp[13],
                                        "ABROAD"           => $tmp[14],
                                        "REQUIREPRESENT"   => $tmp[15],
                                        "SICK"             => $tmp[16],
                                        "ACCIDENTNOTICE"   => $tmp[17],
                                        "NOACCIDENTNOTICE" => $tmp[18],
                                        "PRESENT"          => $tmp[19]
                                        );
                    } elseif ($this->field["KIND"] == 2) { //成績データ
                        //教育課程対応
                        if ($this->Properties["useCurriculumcd"] == '1') {
                            //仮評定フラグ
                            if ($this->Properties["useProvFlg"] == '1') {
                                $data = array(  "SCHOOLCD"         => $tmp[0],
                                                "YEAR"             => $tmp[8],
                                                "SCHREGNO"         => $tmp[4],
                                                "ANNUAL"           => $tmp[9],
                                                "CLASSCD"          => $tmp[14],
                                                "CLASSNAME"        => $tmp[15],
                                                "CLASSABBV"        => $tmp[16],
                                                "CLASSNAME_ENG"    => $tmp[17],
                                                "CLASSABBV_ENG"    => $tmp[18],
                                                "SCHOOL_KIND"      => $tmp[19],
                                                "CURRICULUM_CD"    => $tmp[20],
                                                "SUBCLASSCD"       => $tmp[21],
                                                "SUBCLASSNAME"     => $tmp[22],
                                                "SUBCLASSABBV"     => $tmp[23],
                                                "SUBCLASSNAME_ENG" => $tmp[24],
                                                "SUBCLASSABBV_ENG" => $tmp[25],
                                                "PROV_FLG"         => $tmp[26],
                                                "VALUATION"        => $tmp[27],
                                                "GET_CREDIT"       => $tmp[28],
                                                "ADD_CREDIT"       => $tmp[29],
                                                "COMP_CREDIT"      => $tmp[30],
                                                "REMARK"           => $tmp[31]
                                                );
                            } else {
                                $data = array(  "SCHOOLCD"         => $tmp[0],
                                                "YEAR"             => $tmp[8],
                                                "SCHREGNO"         => $tmp[4],
                                                "ANNUAL"           => $tmp[9],
                                                "CLASSCD"          => $tmp[14],
                                                "CLASSNAME"        => $tmp[15],
                                                "CLASSABBV"        => $tmp[16],
                                                "CLASSNAME_ENG"    => $tmp[17],
                                                "CLASSABBV_ENG"    => $tmp[18],
                                                "SCHOOL_KIND"      => $tmp[19],
                                                "CURRICULUM_CD"    => $tmp[20],
                                                "SUBCLASSCD"       => $tmp[21],
                                                "SUBCLASSNAME"     => $tmp[22],
                                                "SUBCLASSABBV"     => $tmp[23],
                                                "SUBCLASSNAME_ENG" => $tmp[24],
                                                "SUBCLASSABBV_ENG" => $tmp[25],
                                                "VALUATION"        => $tmp[26],
                                                "GET_CREDIT"       => $tmp[27],
                                                "ADD_CREDIT"       => $tmp[28],
                                                "COMP_CREDIT"      => $tmp[29],
                                                "REMARK"           => $tmp[30]
                                                );
                            }
                        } else {
                            //仮評定フラグ
                            if ($this->Properties["useProvFlg"] == '1') {
                                $data = array(  "SCHOOLCD"         => $tmp[0],
                                                "YEAR"             => $tmp[8],
                                                "SCHREGNO"         => $tmp[4],
                                                "ANNUAL"           => $tmp[9],
                                                "CLASSCD"          => $tmp[14],
                                                "CLASSNAME"        => $tmp[15],
                                                "CLASSABBV"        => $tmp[16],
                                                "CLASSNAME_ENG"    => $tmp[17],
                                                "CLASSABBV_ENG"    => $tmp[18],
                                                "SUBCLASSCD"       => $tmp[19],
                                                "SUBCLASSNAME"     => $tmp[20],
                                                "SUBCLASSABBV"     => $tmp[21],
                                                "SUBCLASSNAME_ENG" => $tmp[22],
                                                "SUBCLASSABBV_ENG" => $tmp[23],
                                                "PROV_FLG"         => $tmp[24],
                                                "VALUATION"        => $tmp[25],
                                                "GET_CREDIT"       => $tmp[26],
                                                "ADD_CREDIT"       => $tmp[27],
                                                "COMP_CREDIT"      => $tmp[28],
                                                "REMARK"           => $tmp[29]
                                                );
                            } else {
                                $data = array(  "SCHOOLCD"         => $tmp[0],
                                                "YEAR"             => $tmp[8],
                                                "SCHREGNO"         => $tmp[4],
                                                "ANNUAL"           => $tmp[9],
                                                "CLASSCD"          => $tmp[14],
                                                "CLASSNAME"        => $tmp[15],
                                                "CLASSABBV"        => $tmp[16],
                                                "CLASSNAME_ENG"    => $tmp[17],
                                                "CLASSABBV_ENG"    => $tmp[18],
                                                "SUBCLASSCD"       => $tmp[19],
                                                "SUBCLASSNAME"     => $tmp[20],
                                                "SUBCLASSABBV"     => $tmp[21],
                                                "SUBCLASSNAME_ENG" => $tmp[22],
                                                "SUBCLASSABBV_ENG" => $tmp[23],
                                                "VALUATION"        => $tmp[24],
                                                "GET_CREDIT"       => $tmp[25],
                                                "ADD_CREDIT"       => $tmp[26],
                                                "COMP_CREDIT"      => $tmp[27],
                                                "REMARK"           => $tmp[28]
                                                );
                            }
                        }
                    } else { //観点データ
                        //教育課程対応
                        if ($this->Properties["useCurriculumcd"] == '1') {
                            $data = array(  "YEAR"            => $tmp[0],
                                            "SCHREGNO"        => $tmp[1],
                                            "CLASSCD"         => $tmp[2],
                                            "SCHOOL_KIND"     => $tmp[3],
                                            "CURRICULUM_CD"   => $tmp[4],
                                            "SUBCLASSCD"      => $tmp[5],
                                            "VIEWCD"          => $tmp[6],
                                            "STATUS"          => $tmp[7]
                                            );
                        } else {
                            $data = array(  "YEAR"            => $tmp[0],
                                            "SCHREGNO"        => $tmp[1],
                                            "SUBCLASSCD"      => $tmp[2],
                                            "VIEWCD"          => $tmp[3],
                                            "STATUS"          => $tmp[4]
                                            );
                        }
                    }
                    $data = str_replace("'", "''", $data);
                    if ($this->errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)) {
                        $tempArr[$i] = $data;
                        $i++;
                    }
                    foreach ($primaryKey1 as $key => $val) {
                        $duplicateCsv1[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey2 as $key => $val) {
                        $duplicateCsv2[$line][$val] = $data[$val];
                    }
                    foreach ($primaryKey3 as $key => $val) {
                        $duplicateCsv3[$line][$val] = $data[$val];
                    }
                    $line++;
                }
            }
            $db->commit();
            Query::dbCheckIn($db);
            fclose($fp);
        }
        return $tempArr;
    }

    //エラーチェック
    public function errCheck($db, $data, $checkdata, $line, $duplicateCsv1, $duplicateCsv2, $duplicateCsv3, $primaryKey1, $primaryKey2, $primaryKey3)
    {
        $returnFlg = true;
        $hissuErrFlg        = false;    //必須エラーフラグ
        $lengthErrFlg       = false;    //レングスエラーフラグ
        $duplicateCsvErrFlg = false;    //重複エラーフラグ
        $otherErrFlg        = false;    //その他エラーフラグ
        $existenceErrFlg    = false;    //存在エラーメッセージ

        $hissuErrMsg        = "";       //必須エラーメッセージ
        $lengthErrMsg       = "";       //レングスエラーメッセージ
        $duplicateCsvErrMsg = "";       //重複エラーメッセージ
        $otherErrMsg        = "";       //その他エラーメッセージ
        $existenceErrMsg    = "";       //存在エラーメッセージ

        $hissuSep           = "";
        $lengthSep          = "";
        $duplicateCsvSep    = "";
        $otherSep           = "";
        $existenceSep       = "";

        $dupFlg1 = $this->duplicateCsvCheck($duplicateCsv1, $primaryKey1, $data);
        if ($dupFlg1[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複1(". $dupFlg1[1] ."行目)");
        }

        $dupFlg2 = $this->duplicateCsvCheck($duplicateCsv2, $primaryKey2, $data);
        if ($dupFlg2[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複2(". $dupFlg2[1] ."行目)");
        }

        $dupFlg3 = $this->duplicateCsvCheck($duplicateCsv3, $primaryKey3, $data);
        if ($dupFlg3[0] == 1) {
            $returnFlg = $this->errSet($duplicateCsvErrFlg, $duplicateCsvErrMsg, "※重複(CSV):", $duplicateCsvSep, "重複3(". $dupFlg3[1] ."行目)");
        }

        foreach ($checkdata as $key => $val) {
            if ($this->field["KIND"] == 1) {
                if (in_array($key, array("SCHOOLCD", "SCHREGNO", "YEAR", "ANNUAL"))) {
                    if (!strlen(trim($data[$key]))) {
                        $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                    }
                }
                //日付型チェック
                if (in_array($key, array("SUMDATE"))) {
                    if (common::isDate($data[$key]) == false) {
                        $returnFlg = $this->errSet($otherErrFlg, $otherErrMsg, "※日付(フォーマット):", $otherSep, $val[1]);
                    }
                }
            } elseif ($this->field["KIND"] == 2) {
                if ($this->Properties["useCurriculumcd"] == '1') {
                    if (in_array($key, array("SCHOOLCD", "SCHREGNO", "YEAR", "ANNUAL", "CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD"))) {
                        if (!strlen(trim($data[$key]))) {
                            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                        }
                    }
                } else {
                    if (in_array($key, array("SCHOOLCD", "SCHREGNO", "YEAR", "ANNUAL", "CLASSCD", "SCHOOL_KIND"))) {
                        if (!strlen(trim($data[$key]))) {
                            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                        }
                    }
                }
            } else {
                if ($this->Properties["useCurriculumcd"] == '1') {
                    if (in_array($key, array("YEAR", "SCHREGNO", "CLASSCD", "SCHOOL_KIND", "CURRICULUM_CD", "SUBCLASSCD", "VIEWCD"))) {
                        if (!strlen(trim($data[$key]))) {
                            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                        }
                    }
                } else {
                    if (in_array($key, array("YEAR", "SCHREGNO", "SUBCLASSCD", "VIEWCD"))) {
                        if (!strlen(trim($data[$key]))) {
                            $returnFlg = $this->errSet($hissuErrFlg, $hissuErrMsg, "※必須:", $hissuSep, $val[1]);
                        }
                    }
                }
            }
            //文字数チェック
            if (strlen($data[$key]) > $val[0]) {
                $returnFlg = $this->errSet($lengthErrFlg, $lengthErrMsg, "※サイズ:", $lengthSep, $val[1]);
            }
        }
        //署名チェック
        $cntShomeiZumi = 0;
        if ($this->Properties["useSeitoSidoYorokuShomeiKinou"] == 1) {
            $query = knje061Query::getOpinionsWk($data);
            $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($check["CHAGE_OPI_SEQ"]) {
                $cntShomeiZumi++;
            }
        }
        if ($cntShomeiZumi > 0) {
            $returnFlg = $this->errSet($existenceErrFlg, $existenceErrMsg, "※存在:", $existenceSep, "署名済み");
        }
        if (!$returnFlg) {
            $checkError = $duplicateCsvErrMsg.$hissuErrMsg.$lengthErrMsg.$otherErrMsg.$existenceErrMsg;
            $checkError = $this->checkErrLength($checkError);
            $this->errData[$line]["MSG"] = $checkError;
        }
        return $returnFlg;
    }

    //テンプレートの書き出し
    public function outputTmpFile()
    {
        if ($this->field["KIND"] == 1) {
            $contents  = "※本校区分,";     //SCHOOLCD
            $contents .= "学年,";           //GRADE
            $contents .= "組,";             //HR_CLASS
            $contents .= "出席番号,";       //ATTENDNO
            $contents .= "※学籍番号,";     //SCHREGNO
            $contents .= "生徒氏名,";       //NAME
            $contents .= "※年度,";         //YEAR
            $contents .= "※履修学年,";     //ANNUAL
            $contents .= "集計日付,";       //SUMDATE
            $contents .= "授業日数,";       //CLASSDAYS
            $contents .= "休学日数,";       //OFFDAYS
            $contents .= "公欠日数,";       //ABSENT
            $contents .= "出停日数,";       //SUSPEND
            $contents .= "忌引日数,";       //MOURNING
            $contents .= "留学日数,";       //ABROAD
            $contents .= "要出席日数,";     //REQUIREPRESENT
            $contents .= "病欠日数,";       //SICK
            $contents .= "事故欠届日数,";   //ACCIDENTNOTICE
            $contents .= "事故欠無日数,";   //NOACCIDENTNOTICE
            $contents .= "出席日数,";       //PRESENT
            $contents .= $this->lastColumn."\n";

            $contents .= "0,";              //SCHOOLCD
            $contents .= "01,";             //GRADE
            $contents .= "001,";            //HR_CLASS
            $contents .= "001,";            //ATTENDNO
            $contents .= "20051003,";       //SCHREGNO
            $contents .= "今井花子,";       //NAME
            $contents .= "2005,";           //YEAR
            $contents .= "01,";             //ANNUAL
            $contents .= "2009/07/10,";     //SUMDATE
            $contents .= "191,";            //CLASSDAYS
            $contents .= "0,";              //OFFDAYS
            $contents .= "0,";              //ABSENT
            $contents .= "1,";              //SUSPEND
            $contents .= "1,";              //MOURNING
            $contents .= "0,";              //ABROAD
            $contents .= "189,";            //REQUIREPRESENT
            $contents .= "1,";              //SICK
            $contents .= "1,";              //ACCIDENTNOTICE
            $contents .= "1,";              //NOACCIDENTNOTICE
            $contents .= "186,";            //PRESENT
            $contents .= $this->lastColumn;

            $filename = sprintf("%s.csv", "学籍出欠記録データ");
        } elseif ($this->field["KIND"] == 2) {
            $contents  ="※本校区分,学年,組,出席番号,※学籍番号,生徒氏名,性別区分,性別,";
            $contents .="※年度,※履修学年,課程コード,学科コード,コースコード,コースコード名,";
            $contents .="※教科コード,教科名,教科略称,教科名英字,教科略称英字,";
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $contents .= "※学校種別,";         //SCHOOL_KIND
                $contents .= "※教育課程コード,";   //CURRICULUM_CD
            }
            $contents .="※科目コード,科目名,科目略称,科目名英字,科目略称英字,";
            //仮評定フラグ
            if ($this->Properties["useProvFlg"] == '1') {
                $contents .= "仮評定フラグ,";   //PROV_FLG
            }
            $contents .="評価,標準修得単位,増加修得単位,履修単位,履修科目備考,LASTCOLUMN\n";
            $contents .="0,01,001,001,20051003,今井花子,2,女性,";
            $contents .="2005,01,1,001,0001,文Ⅰ,";
            $contents .="13,公民,公民,Civics,Civ,";
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                //学校種別ヘッダ例取得(始)
                $db = Query::dbCheckOut();
                $schoolKindRei = "";
                $result = $db->query(knje061Query::getSchoolKind());
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($schoolKindRei != "") {
                        $schoolKindRei .= " ";
                    }
                    $schoolKindRei .= $row["NAME1"].":".$row["ABBV1"];
                }
                $result->free();
                if ($schoolKindRei == "") {
                    $schoolKindRei = "名称マスタ未設定";
                }
                Query::dbCheckIn($db);
                //学校種別ヘッダ例取得(終)
                $contents .= "{$schoolKindRei},";   //SCHOOL_KIND
                $contents .= "2,";                  //CURRICULUM_CD
            }
            $contents .="130001,現代社会,現代社,Civics,Civ,";
            //仮評定フラグ
            if ($this->Properties["useProvFlg"] == '1') {
                $contents .= "1,";   //PROV_FLG
            }
            $contents .="2,2,2,2,備考です,".$this->lastColumn;

            $filename = sprintf("%s.csv", "学籍学習記録データ");
        } else {
            $contents  = "※年度,";         //YEAR
            $contents .= "※学籍番号,";     //SCHREGNO
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                $contents .= "※教科コード,";       //CLASSCD
                $contents .= "※学校種別,";         //SCHOOL_KIND
                $contents .= "※教育課程コード,";   //CURRICULUM_CD
            }
            $contents .= "※科目コード,";   //SUBCLASSCD
            $contents .= "※観点コード,";   //VIEWCD
            $contents .= "観点,";           //STATUS
            $contents .= $this->lastColumn."\n";

            $contents .= "2005,";           //YEAR
            $contents .= "20051003,";       //SCHREGNO
            //教育課程対応
            if ($this->Properties["useCurriculumcd"] == '1') {
                //学校種別ヘッダ例取得(始)
                $db = Query::dbCheckOut();
                $schoolKindRei = "";
                $result = $db->query(knje061Query::getSchoolKind());
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($schoolKindRei != "") {
                        $schoolKindRei .= " ";
                    }
                    $schoolKindRei .= $row["NAME1"].":".$row["ABBV1"];
                }
                $result->free();
                if ($schoolKindRei == "") {
                    $schoolKindRei = "名称マスタ未設定";
                }
                Query::dbCheckIn($db);
                //学校種別ヘッダ例取得(終)
                $contents .= "13,";                 //CLASSCD
                $contents .= "{$schoolKindRei},";   //SCHOOL_KIND
                $contents .= "2,";                  //CURRICULUM_CD
            }
            $contents .= "130001,";         //SUBCLASSCD
            $contents .= "1301,";           //VIEWCD
            $contents .= "A,";              //STATUS
            $contents .= $this->lastColumn;

            $filename = sprintf("%s.csv", "観点データ");
        }

        common::downloadContents($contents, $filename);
        return true;
    }

    /****************************/
    /* ↓↓↓ 観点データ ↓↓↓ */
    /****************************/

    //メイン
    public function updateKanten()
    {
        //ＤＢ接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knje061Query::getStudent($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            $year = $row["YEAR"];
            $grade = $row["GRADE"];
            //削除
            knje061Query::delDataJview($db, $schregNo, $this, $year);
            // 立命館小学
            if ($this->schoolName == "Ritsumeikan") {
                //通知票観点データから指導要録観点データを生成
                knje061Query::insDataJviewRitsumeikanP($db, $schregNo, $this, $year, $grade);
                //指導要録観点データから学習記録データを生成
                if ($this->Properties["useKantenToStudyRec"] == '1') {
                    //削除
                    knje061Query::delDataStudyRecRitsumeikanP($db, $schregNo, $this, $year);
                    //追加
                    knje061Query::insDataStudyRecRitsumeikanP($db, $schregNo, $this, $year, $grade);
                }
                // 駿台甲府小学
            } elseif ($this->schoolName == "sundaikoufu" && $row["SCHOOL_KIND"] == "P") {
                //通知票観点データから指導要録観点データを生成
                knje061Query::insDataJviewSundaiP($db, $schregNo, $this, $year, $grade);
                //指導要録観点データから学習記録データを生成
                if ($this->Properties["useKantenToStudyRec"] == '1') {
                    //削除
                    knje061Query::delDataStudyRecSundaiP($db, $schregNo, $this, $year);
                    //追加
                    knje061Query::insDataStudyRecSundaiP($db, $schregNo, $this, $year, $grade);
                }

                // 武蔵野東の場合
            } elseif ($this->schoolName == "musashinohigashi") {
                //追加
                knje061Query::insDataJviewMusasinoHigasi($db, $schregNo, $this, $year, $grade);
                if ($this->Properties["useKantenToStudyRec"] == '1') {
                    //削除
                    knje061Query::delDataStudyRecMusasinoHigasi($db, $schregNo, $this, $year);
                    //追加
                    knje061Query::insDataStudyRecMusasinoHigasi($db, $schregNo, $this, $year, $grade);
                }
                // 大阪信愛の小学校の場合
            } elseif ($this->schoolName == "osakashinnai" && $row["SCHOOL_KIND"] == "P") {
                //追加
                knje061Query::insDataJviewOsakashinnai($db, $schregNo, $this, $year, $grade);

                // 観点の計算方法 3:設定(JVIEWSTAT_LEVEL_MSTを参照)
            } elseif ($this->keisanDiv == "3") {
                //追加(明治用)
                knje061Query::insDataJviewMeiji($db, $schregNo, $this, $year, $grade);
            } else {
                //追加
                knje061Query::insDataJview($db, $schregNo, $this, $year, $grade);
            }
        }

        $db->commit();
        //ＤＢ切断
        Query::dbCheckIn($db);
    }

    //観点データ生成（大宮）
    public function updateKantenOomiya()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //生徒
        $query = knje061Query::getStudent($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            $year = $row["YEAR"];
            //削除
            knje061Query::delDataJview($db, $schregNo, $this, $year);

            //学籍学習記録データの評定から観点データをランダム生成
            knje061Query::getSchregStudyrecDat($db, $this, $schregNo, $year);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //観点データ生成（海城）
    public function updateKantenKaijou()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //生徒
        $query = knje061Query::getStudent($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            $year = $row["YEAR"];
            //削除
            knje061Query::delDataJview($db, $schregNo, $this, $year);

            //成績データの学年成績から観点データを生成
            knje061Query::getRecordScoreDat($db, $this, $schregNo, $year);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //観点データ生成（玉川聖）(成城)
    public function updateKantenTamagawa()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //元になるテストCD取得・・・成績から観点を生成する処理に使用
        $e064Row = $db->getRow(knje061Query::getNameMstE064(), DB_FETCHMODE_ASSOC);
        $this->e064name1 = $e064Row["NAME1"];

        //生徒
        $query = knje061Query::getStudent($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            $year = $row["YEAR"];
            //削除
            knje061Query::delDataJview($db, $schregNo, $this, $year);

            //成績データから観点データを生成（観点変換テーブル参照）
            knje061Query::getRecordScoreDatTamagawa($db, $this, $schregNo, $year);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    /******************************/
    /* ↓↓↓ 行動の記録他 ↓↓↓ */
    /******************************/

    //行動の記録他データ生成　駿台から要望
    public function updateBehavior()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //生徒
        $query = knje061Query::getStudent($this);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregNo = $row["SCHREGNO"];
            $year = $row["YEAR"];
            $annual = $row["ANNUAL"];
            $grade = $row["GRADE"];
            $schoolKind = $row["SCHOOL_KIND"];

            //BEHAVIOR_DATを生成
            //1)行動の記録　BEHAVIOR_SEMES_DATからBEHAVIOR_DATへ
            knje061Query::getBehaviorDat($db, $this, $schregNo, $year, $annual, $grade);

            if ($this->schoolName != "musashinohigashi") {
                //HTRAINREMARK_DATを生成
                //2)総合的な学習の時間　RECORD_TOTALSTUDYTIME_DATからHTRAINREMARK_DATへ
                //3)出欠　SCHREG_ATTENDREC_DATからHTRAINREMARK_DATへ
                knje061Query::getHtrainremarkDat($db, $this, $schregNo, $year, $annual, $schoolKind);
            }
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }
}

<?php

require_once('for_php7.php');

class knjd186vModel extends Model {
    var $semester;
    var $hrClass;
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $selectdata;

    function knjd186vModel() {
        common::GetControlMaster_Fnc($this->control);
    }

    function init() {
        $this->semester = VARS::post("SEMESTER");
        $this->hrClass  = VARS::post("GRADE_HR_CLASS");
        $this->cmd      = VARS::request("cmd");

        if ($this->cmd == '') {
            unset($this->semester);
            unset($this->hrClass);
        }
    
        $this->field = array("DATE"             =>  VARS::post("DATE"),              //異動対象日付
                             "YEAR"             =>  VARS::post("YEAR"),              //年度
                             "SEMESTER"         =>  VARS::post("SEMESTER"),          //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),    //年組
                             "GROUP_DIV"        =>  VARS::post("GROUP_DIV"),         //グループ区分
                             "MAX_OR_SIDOU"     =>  VARS::post("MAX_OR_SIDOU"),      //最高点・追指導
                             "HOGOSHA"          =>  VARS::post("HOGOSHA"),           //保護者からのコメント欄・出欠の記録（考査ごと）
                             "RANK_DIV"         =>  VARS::post("RANK_DIV"),          //順位の基準点
                             "PATARN_DIV"       =>  VARS::post("PATARN_DIV"),        //帳票パターン
                             "NOT_PRINT_SUBEKI" =>  VARS::post("NOT_PRINT_SUBEKI"),  //出席すべき時数表示なし
                             "NOT_PRINT_GAKUNEN_HYOKA_HYOTEI" =>  VARS::post("NOT_PRINT_GAKUNEN_HYOKA_HYOTEI"),  //合併元科目の学年評価・学年評定・出欠時数なし
                             "CHIKOKU_SOUTAI_NASI" =>  VARS::post("CHIKOKU_SOUTAI_NASI"),  //遅刻・早退 表示なし
                             "NO_ATTEND_SUBCLASS_SP" =>  VARS::post("NO_ATTEND_SUBCLASS_SP"),  //LHR、生徒会活動、学校行事表示なし
                             "PRINT_CAREERPLAN" =>  VARS::post("PRINT_CAREERPLAN"),  //キャリアプラン表示あり
                             "NOT_PRINT_LASTEXAM"  =>  VARS::post("NOT_PRINT_LASTEXAM"),        //最終考査表記しない
                             "NOT_PRINT_LASTEXAM_SCORE"  =>  VARS::post("NOT_PRINT_LASTEXAM_SCORE"),        //最終考査表記しない 成績のみ
                             "NO_PRINT_SEMENAME_IN_TESTNAME"  =>  VARS::post("NO_PRINT_SEMENAME_IN_TESTNAME"),  //考査名に学期名表示なし
                             "NO_PRINT_RANK"  =>  VARS::post("NO_PRINT_RANK"),  //順位表記なし
                             "PRINT_TUISHIDOU"  =>  VARS::post("PRINT_TUISHIDOU"),  //追指導表示
                             "editGroupDivCFlg" =>  VARS::post("editGroupDivCFlg"),        //帳票パターンでCパターンを選択したか
                             "ADD_PAST_CREDIT"  =>  VARS::post("ADD_PAST_CREDIT"),  //修得単位数
                             "NO_PRINT_GAKUNENHYOKA_HYOTEI_SUM_AVG" =>  VARS::post("NO_PRINT_GAKUNENHYOKA_HYOTEI_SUM_AVG"),        //学年評価・評定の合計点・平均点表記なし
                             "NO_PRINT_HOGOSHA" =>  VARS::post("NO_PRINT_HOGOSHA"),        //保護者欄
                             "NO_PRINT_COMMUNICATION" =>  VARS::post("NO_PRINT_COMMUNICATION"),        // 通信欄
                             "NO_PRINT_ATTENDREMARK" =>  VARS::post("NO_PRINT_ATTENDREMARK"),        // 出欠備考
                             "KETTEN_KAMOKU_NO_SUBTRACT" =>  VARS::post("KETTEN_KAMOKU_NO_SUBTRACT"),        // 欠点科目数表記
                             "ZOUKA" =>  VARS::post("ZOUKA"),        // 増加単位 加算する
                             "DBNAME"           =>  VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd          = VARS::request("cmd");
        $this->selectdata   = VARS::post("selectdata");

        if (!$this->field["SEMESTER"]) {
            $this->field["SEMESTER"] = CTRL_SEMESTER;
        }

        if (!$this->semester) {
            $this->semester = CTRL_SEMESTER;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    function clean()
    {
    }
}
?>

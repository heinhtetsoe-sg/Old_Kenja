<?php

require_once('for_php7.php');

class knji060dModel extends Model
{
    public $field      = array();
    public $cmd;
    public $control;       //コントロールマスタ
    public $school_name;   //学校名

    /**
     * メンバ変数の初期化
     */
    public function knji060dModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("DATE"            =>  VARS::post("DATE"),          //処理年月日
                            "OUTPUT"           =>  VARS::post("OUTPUT"),        //調査書種類
                            "OS"               =>  VARS::post("OS"),            //所見入力時ＯＳ
                            "KANJI"            =>  VARS::post("KANJI"),         //漢字氏名印刷
                            "MIRISYU"          =>  VARS::post("MIRISYU"),       //未履修科目出力
                            "RISYU"            =>  VARS::post("RISYU"),         //履修のみ科目出力
                            "FORM6"            =>  VARS::post("FORM6"),         //６年用フォーム選択
                            "useSyojikou3"     =>  VARS::post("useSyojikou3"),  //指導上参考となる諸事欄、３分割フォーム
                            "tyousasyoNotPrintAnotherAttendrec"    =>  VARS::post("tyousasyoNotPrintAnotherAttendrec"),    //出欠の前籍校を含まないチェックボックスを作成する
                            "HYOTEI"           =>  VARS::post("HYOTEI"),        //評定の処理設定
                            "GVAL_CALC_CHECK"  =>  VARS::post("GVAL_CALC_CHECK"),  //評定平均席次出力
                            "PRINT_AVG_RANK"   =>  VARS::post("PRINT_AVG_RANK"),  //評定平均算出
                            "COMMENT"          =>  VARS::post("COMMENT"),       //評定の処理設定
                            "TANIPRINT_SOUGOU" =>  VARS::post("TANIPRINT_SOUGOU"), //総合的な学習の時間の単位を０表示
                            "TANIPRINT_RYUGAKU" => VARS::post("TANIPRINT_RYUGAKU"), //留学の単位を０表示
                            "COMMENT"          =>  VARS::post("COMMENT"),       //評定の処理設定
                            "KNJE070D_PRINTHEADERNAME" => VARS::post("KNJE070D_PRINTHEADERNAME"), // 偶数頁の氏名出力
                            "OUTPUT_PRINCIPAL" =>  VARS::post("OUTPUT_PRINCIPAL"), // 校長印刷
                            "KNJE070_CHECK_PRINT_STAMP_PRINCIPAL" =>  VARS::post("KNJE070_CHECK_PRINT_STAMP_PRINCIPAL"), // 校長印出力
                            "KNJE070_CHECK_PRINT_STAMP_HR_STAFF" =>  VARS::post("KNJE070_CHECK_PRINT_STAMP_HR_STAFF"), // 記載責任者印出力
                            "SEKI"             =>  VARS::post("SEKI"),          //記載責任者
                            "DBNAME"           =>  VARS::post("DBNAME")         //DB変更に備えて
                     );


        $this->cmd         = VARS::request("cmd");

        $db = Query::dbCheckOut();
        $query = knji060dQuery::getSchoolName();
        $this->school_name = $db->getOne($query);
        Query::dbCheckIn($db);

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
        //初期値の設定
        $this->tyousasyoAttendrecRemarkFieldSize    = $this->Properties["tyousasyoAttendrecRemarkFieldSize"];
        $this->Properties["tyousasyoTaniPrint"]     = ($this->Properties["tyousasyoTaniPrint"] == '2')  ? $this->Properties["tyousasyoTaniPrint"]   : 1;
        $this->Properties["tyousasyoKinsokuForm"]   = ($this->Properties["tyousasyoKinsokuForm"])       ? $this->Properties["tyousasyoKinsokuForm"] : "";
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}

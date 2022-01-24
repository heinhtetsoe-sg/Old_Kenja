<?php

require_once('for_php7.php');

class knji050Model extends Model
{
    public $field      = array();
    public $cmd;
    public $control;       //コントロールマスタ
    public $school_name;   //学校名

    /**
     * メンバ変数の初期化
     */
    public function knji050Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    public function init()
    {
        $this->field = array("DATE"             =>  VARS::post("DATE"),             //処理年月日
                             "YEAR"             =>  VARS::post("YEAR"),             //年度
                             "OUTPUT"           =>  VARS::post("OUTPUT"),           //出力選択  //NO002
                             "GAKKI"            =>  VARS::post("GAKKI"),            //学期コード
                             "GRADE_HR_CLASS"   =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "seito"            =>  VARS::post("seito"),            //帳票種別（生徒指導要録）
                             "koseki"           =>  VARS::post("koseki"),           //帳票種別（戸籍氏名出力）
                             "simei"            =>  VARS::post("simei"),            //帳票種別（生徒・保護者氏名出力）
                             "inei"             =>  VARS::post("inei"),             //帳票種別（印影出力）
                             "schzip"           =>  VARS::post("schzip"),           //帳票種別（生徒郵便番号）
                             "addr2"            =>  VARS::post("addr2"),            //帳票種別（方書き）
                             "schoolzip"        =>  VARS::post("schoolzip"),        //帳票種別（学校郵便番号）
                             "katsudo"          =>  VARS::post("katsudo"),          //帳票種別（活動の記録）
                             "gakushu"          =>  VARS::post("gakushu"),          //帳票種別（学習の記録）
                             "tani"             =>  VARS::post("tani"),             //帳票種別（習得単位の記録）
                             "MIRISYU"          =>  VARS::post("MIRISYU"),          //未履修科目出力
                             "RISYU"            =>  VARS::post("RISYU"),            //履修のみ科目出力
                             "RISYUTOUROKU"     =>  VARS::post("RISYUTOUROKU"),     //履修登録のみ科目出力
                             "DBNAME"           =>  VARS::post("DBNAME")            //DB変更に備えて
                     );

        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));

        $this->cmd         = VARS::request("cmd");
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        //初期値の設定
        $this->Properties["useSchregRegdHdat"]                        = $this->Properties["useSchregRegdHdat"]                        ? $this->Properties["useSchregRegdHdat"]                        : 0;
        $this->Properties["seitoSidoYorokuFormType"]                  = $this->Properties["seitoSidoYorokuFormType"]                  ? $this->Properties["seitoSidoYorokuFormType"]                  : 4;
        $this->Properties["seitoSidoYorokuFieldSize"]                 = $this->Properties["seitoSidoYorokuFieldSize"]                 ? $this->Properties["seitoSidoYorokuFieldSize"]                 : 0;
        $this->Properties["seitoSidoYorokuSougouFieldSize"]           = $this->Properties["seitoSidoYorokuSougouFieldSize"]           ? $this->Properties["seitoSidoYorokuSougouFieldSize"]           : 0;
        $this->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] = $this->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] ? $this->Properties["seitoSidoYorokuSpecialactremarkFieldSize"] : 0;
        $this->Properties["seitoSidoYorokuKinsokuForm"]               = $this->Properties["seitoSidoYorokuKinsokuForm"]               ? $this->Properties["seitoSidoYorokuKinsokuForm"]               : 0;
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}

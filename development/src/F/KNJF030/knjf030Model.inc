<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjf030Model extends Model
{
    public $field      = array();
    public $cmd;
    public $control;       //コントロールマスタ
    public $staffcd;       //職員コード
    public $select_opt  = array(); //セレクトデータ
    public $selectleft;
    public $schoolName;

    /**
     * メンバ変数の初期化
     */
    public function knjf030Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;                           //職員コード
    }

    public function init()
    {
        $this->field = array("YEAR"                     =>  VARS::post("YEAR"),                     //年度
                            "GAKKI"                     =>  VARS::post("GAKKI"),                    //学期コード
                            "KUBUN"                     =>  VARS::post("KUBUN"),                    //クラス・個人表示指定
                            "GRADE_HR_CLASS"            =>  VARS::post("GRADE_HR_CLASS"),           //クラス
                            "DATE"                      =>  VARS::post("DATE"),                     //
                            "DATE5"                     =>  VARS::post("DATE5"),                    //
                            "CHECK1"                    =>  VARS::post("CHECK1"),                   //検診票（一般）チェックボックス
                            "OUTPUTA"                   =>  VARS::post("OUTPUTA"),                  //出力方法オプションボタン
                            "PRINT_STAMP"               =>  VARS::post("PRINT_STAMP"),              //印影出力
                            "PRINT_STAMP2"              =>  VARS::post("PRINT_STAMP2"),             //印影出力
                            "PRINT_SCHREGNO1"           =>  VARS::post("PRINT_SCHREGNO1"),          //学籍番号出力
                            "PRINT_SCHREGNO2"           =>  VARS::post("PRINT_SCHREGNO2"),          //学籍番号出力
                            "useForm9_PJ_Ippan"         =>  VARS::post("useForm9_PJ_Ippan"),        //9年用フォーム（小中のみ）
                            "useForm7_JH_Ippan"         =>  VARS::post("useForm7_JH_Ippan"),        //7年用フォーム（中高のみ）
                            "CHECK2"                    =>  VARS::post("CHECK2"),                   //検診票（歯口腔）チェックボックス
                            "OUTPUTB"                   =>  VARS::post("OUTPUTB"),                  //出力方法オプションボタン
                            "useForm9_PJ_Ha"            =>  VARS::post("useForm9_PJ_Ha"),           //9年用フォーム（小中のみ）
                            "useForm7_JH_Ha"            =>  VARS::post("useForm7_JH_Ha"),           //7年用フォーム（中高のみ）
                            "CHECK1_2"                  =>  VARS::post("CHECK1_2"),                 //両面印刷
                            "CHECK3"                    =>  VARS::post("CHECK3"),                   //未検診生徒チェックボックス
                            "MIJUKEN_ITEM01"            =>  VARS::post("MIJUKEN_ITEM01"),           //未検診項目
                            "MIJUKEN_ITEM02"            =>  VARS::post("MIJUKEN_ITEM02"),           //未検診項目
                            "MIJUKEN_ITEM03"            =>  VARS::post("MIJUKEN_ITEM03"),           //未検診項目
                            "MIJUKEN_ITEM04"            =>  VARS::post("MIJUKEN_ITEM04"),           //未検診項目
                            "MIJUKEN_ITEM05"            =>  VARS::post("MIJUKEN_ITEM05"),           //未検診項目
                            "MIJUKEN_ITEM06"            =>  VARS::post("MIJUKEN_ITEM06"),           //未検診項目
                            "CHECK4"                    =>  VARS::post("CHECK4"),                   //眼科受診チェックボックス
                            "CHECK5"                    =>  VARS::post("CHECK5"),                   //検診結果チェックボックス
                            "CHECK6"                    =>  VARS::post("CHECK6"),                   //健康診断結果チェックボックス
                            "OUTPUT"                    =>  VARS::post("OUTPUT"),                   //出力方法オプションボタン
                            "CHECK7"                    =>  VARS::post("CHECK7"),                   //定期健康診断チェックボックス
                            "STANDARD_NOTSHOW"          =>  VARS::post("STANDARD_NOTSHOW"),         //標準体重・肥満度チェックボックス
                            "FAMILY_CONTACT_COMMENT"    =>  VARS::post("FAMILY_CONTACT_COMMENT"),   //家庭連絡コメントチェックボックス
                            "DOCUMENTCD"                =>  VARS::post("DOCUMENTCD"),               //文面コンボ
                            "CHECK8"                    =>  VARS::post("CHECK8"),                   //内科検診チェックボックス
                            "CHECK9"                    =>  VARS::post("CHECK9"),                   //異常者一覧チェックボックス
                            "CHECK10"                   =>  VARS::post("CHECK10"),                  //尿検査結果のお知らせチェックボックス
                            "CHECK11"                   =>  VARS::post("CHECK11"),                  //視力の検査結果のお知らせチェックボックス
                            "CHECK12"                   =>  VARS::post("CHECK12"),                  //聴力の検査結果のお知らせチェックボックス
                            "SELECT1"                   =>  VARS::post("SELECT1"),                  //一般条件リスト
                            "SELECT2"                   =>  VARS::post("SELECT2"),                  //歯口腔条件リスト
                            "URINALYSIS_OUTPUT"         =>  VARS::post("URINALYSIS_OUTPUT"),        //尿検査診断結果チェックボックス
                            "URINALYSIS_CHECK"          =>  VARS::post("URINALYSIS_CHECK"),         //尿検査診断結果 検査結果ラジオボタン
                            "DBNAME"                    =>  VARS::post("DBNAME")                    //DB変更に備えて
                     );

        $this->cmd         = VARS::request("cmd");

        $this->selectleft = VARS::post("selectleft");
        if ($this->cmd != 'change_class') {
            $this->select_opt = array();
        }

        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    /**
     * 変数をクリアします
     */
    public function clean()
    {
    }
}

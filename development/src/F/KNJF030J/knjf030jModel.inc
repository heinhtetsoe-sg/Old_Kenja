<?php

require_once('for_php7.php');

class knjf030jModel extends Model
{
    public $field = array();
    public $cmd;
    public $control;             //コントロールマスタ
    public $staffcd;             //職員コード
    public $select_opt = array();//セレクトデータ
    public $selectleft;
    public $schoolName;

    /**
     * メンバ変数の初期化
     */
    public function knjf030jModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->control["学籍処理日"] = str_replace("-", "/", $this->control["学籍処理日"]);
        $this->staffcd = STAFFCD;//職員コード
    }

    public function init()
    {
        $this->field = array(
            "YEAR"           => VARS::post("YEAR"),          //年度
            "GAKKI"          => VARS::post("GAKKI"),         //学期コード
            "KUBUN"          => VARS::post("KUBUN"),         //クラス・個人表示指定
            "GRADE_HR_CLASS" => VARS::post("GRADE_HR_CLASS"),//クラス

            "CHECK1"         => VARS::post("CHECK1"),        //検診票（一般）チェックボックス
            "RADIO1"         => VARS::post("RADIO1"),        //検診票（一般）　右のラジオボタン
            "CHECK2"         => VARS::post("CHECK2"),        //検診票（歯口腔）チェックボックス
            "RADIO2"         => VARS::post("RADIO2"),        //検診票（歯口腔）右のラジオボタン
            "CHECK1_2"       => VARS::post("CHECK1_2"),      //両面印刷

            "SEND_DATE"      => VARS::post("SEND_DATE"),     //提出日
            "CHECK3"         => VARS::post("CHECK3"),        //検診結果通知書（一覧）　チェックボックス
            "CHECK4"         => VARS::post("CHECK4"),        //検診結果通知書（運動器）チェックボックス
            "CHECK5"         => VARS::post("CHECK5"),        //検診結果通知書（眼科）　チェックボックス
            "CHECK6"         => VARS::post("CHECK6"),        //検診結果通知書（結核）　チェックボックス
            "CHECK7"         => VARS::post("CHECK7"),        //検診結果通知書（視力）　チェックボックス
            "CHECK8"         => VARS::post("CHECK8"),        //検診結果通知書（歯科）　チェックボックス
            "CHECK9"         => VARS::post("CHECK9"),        //検診結果通知書（耳鼻科）チェックボックス
            "CHECK10"        => VARS::post("CHECK10"),       //検診結果通知書（色覚）　チェックボックス
            "CHECK11"        => VARS::post("CHECK11"),       //検診結果通知書（心電図）チェックボックス
            "CHECK12"        => VARS::post("CHECK12"),       //検診結果通知書（聴力）　チェックボックス
            "CHECK13"        => VARS::post("CHECK13"),       //検診結果通知書（内科）　チェックボックス
            "CHECK14"        => VARS::post("CHECK14"),       //検診結果通知書（尿）　　チェックボックス

            "RADIO3"         => VARS::post("RADIO3"),        //検診結果通知書（一覧）　右のラジオボタン
            "RADIO4"         => VARS::post("RADIO4"),        //検診結果通知書（運動器）右のラジオボタン
            "RADIO5"         => VARS::post("RADIO5"),        //検診結果通知書（眼科）　右のラジオボタン
            "RADIO6"         => VARS::post("RADIO6"),        //検診結果通知書（結核）　右のラジオボタン
            "RADIO7"         => VARS::post("RADIO7"),        //検診結果通知書（視力）　右のラジオボタン
            "RADIO8"         => VARS::post("RADIO8"),        //検診結果通知書（歯科）　右のラジオボタン
            "RADIO9"         => VARS::post("RADIO9"),        //検診結果通知書（耳鼻科）右のラジオボタン
            "RADIO10"        => VARS::post("RADIO10"),       //検診結果通知書（色覚）　右のラジオボタン
            "RADIO11"        => VARS::post("RADIO11"),       //検診結果通知書（心電図）右のラジオボタン
            "RADIO12"        => VARS::post("RADIO12"),       //検診結果通知書（聴力）　右のラジオボタン
            "RADIO13"        => VARS::post("RADIO13"),       //検診結果通知書（内科）　右のラジオボタン
            "RADIO14"        => VARS::post("RADIO14"),       //検診結果通知書（尿）　　右のラジオボタン

            "CHECK15"        => VARS::post("CHECK15"),       //一次検査の±を再検査対象とする　チェックボックス
            "CHECK16"        => VARS::post("CHECK16"),       //再検査の±を再検査対象とする　　チェックボックス

            "DBNAME"         => VARS::post("DBNAME")         //DB変更に備えて
        );

        $this->cmd = VARS::request("cmd");

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

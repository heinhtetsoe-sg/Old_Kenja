<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

class knjg050Model extends Model
{
    public $field = array();
    public $cmd;
    public $control;   //コントロールマスタ
    public $output;    //切替ラジオ

    /**
     * メンバ変数の初期化
     */
    public function knjg050Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {
        $this->field = array("YEAR"          => VARS::post("YEAR"),           //年度
                            "CMBCLASS"       => VARS::post("CMBCLASS"),       //学年・クラス
                            "NOTICEDAY"      => VARS::post("NOTICEDAY"),      //記載日付
                            "GAKKI"          => VARS::post("GAKKI"),          //学期コード
                            "SOTUGYO_MIKOMI" => VARS::post("SOTUGYO_MIKOMI"), //卒業見込み出力
                            "ENT_GRD_DATE_FORMAT" =>  VARS::post("ENT_GRD_DATE_FORMAT"),  //入学・卒業日付は年月で表示する
                            "PRINT_STAMP"    => VARS::post("PRINT_STAMP"),    //印影出力する
                            "DBNAME"         => VARS::post("DBNAME")          //DB変更に備えて
                     );

        $this->cmd    = VARS::request("cmd");
        $this->output = VARS::post("OUTPUT");

        $this->getPropertiesAll(); //プロパティファイルの読込み
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

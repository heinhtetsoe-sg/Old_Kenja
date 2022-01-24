<?php

require_once('for_php7.php');

class knje080aModel extends Model
{
    public $field       = array();
    public $select_data = array();
    public $cmd;
    public $control;   //コントロールマスタ

    public function knje080aModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    public function init()
    {


        $this->field = array("DATE"               =>  VARS::post("DATE"),             //処理年月日
                             "YEAR"               =>  VARS::post("YEAR"),             //年度
                             "GAKKI"              =>  VARS::post("GAKKI"),            //学期コード
                             "GRADE_HR_CLASS"     =>  VARS::post("GRADE_HR_CLASS"),   //年組
                             "NOT_PRINT_CERTIFNO" =>  VARS::post("NOT_PRINT_CERTIFNO"), //発行番号は印刷しない
                             "USE_CERTIFNO_START" =>  VARS::post("USE_CERTIFNO_START"), //印刷する発行番号を指定する
                             "CERTIFNO_START"     =>  VARS::post("CERTIFNO_START"),     //開始発行番号
                             "ATTEND_START_DATE"  =>  VARS::post("ATTEND_START_DATE"),  //出欠集計範囲
                             "ATTEND_END_DATE"    =>  VARS::post("ATTEND_END_DATE"),    //出欠集計範囲
                             "NOT_PRINT_RISHUTYU" =>  VARS::post("NOT_PRINT_RISHUTYU"), //履修中は印刷しない
                             "PRINT_RI"           =>  VARS::post("PRINT_RI"),          //履修中は「履」を印刷する。
                             "BIKO"               =>  VARS::post("BIKO"),               //備考欄へ出力する内容
                             "PRINT_SOTSU"        =>  VARS::post("PRINT_SOTSU"),        //在学生を卒業と出力する。
                             "PRINT_GRD_DATE"     =>  VARS::post("PRINT_GRD_DATE"),     //卒業日
                             "REGDDIV"            =>  VARS::post("REGDDIV"),          //卒業生選択
                             "GRD_YEAR_SEMESTER"  =>  VARS::post("GRD_YEAR_SEMESTER"),  //卒業生年度学期
                             "DBNAME"             =>  VARS::post("DBNAME")            //DB変更に備えて
                            );


        $this->select_data["selectdata"] = explode(",", VARS::post("selectdata"));
        $this->cmd = VARS::request("cmd");

        //プロパティファイル
        $this->getPropertiesAll();
        //初期値の設定
        $this->Knje080UseAForm = ($this->Properties["Knje080UseAForm"] == '1') ? $this->Properties["Knje080UseAForm"] : 0;
    }
}

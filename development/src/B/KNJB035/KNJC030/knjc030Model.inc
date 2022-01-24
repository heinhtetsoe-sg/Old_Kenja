<?php

require_once('for_php7.php');

/**
 * 定数定義
*/

//配列内の数値のみ取得
function num_filter($var) {
    return (is_numeric($var));
}
class knjc030Model extends Model {
    var $cmd;
    var $control_data = array();            //学籍処理日
    var $cntl_dt_year;                      //学籍処理日の年(処理日が無ければウィンドウを閉じる)
    var $cntl_dt_key;                       //コントロールデートキー
    var $executedate;                       //カレンダーコントロール用データ
    var $tmp_key;                           //カレンダーコントロール用データ一時保持
    var $staffcd;                           //スタッフコード保持
    var $attnd_cntl_dt;                     //出力制御日付  (制御日付外は管理者のみ)
    var $cntl_semester;                     //現在の学期を保持

    /**
     * メンバ変数の初期化
     */
    function knjc030Model()
    {
        //学籍処理日取得
        $this->attnd_cntl_dt  = ATTEND_CTRL_DATE;
        $this->cntl_semester  = CTRL_SEMESTER;
        $this->cntl_dt_key    = str_replace("/","-",CTRL_DATE);
        $this->cntl_dt_year   = CTRL_YEAR;
    }

    function init()
    {
        //control year semester 初期化
        $this->cntl_dt_year  = CTRL_YEAR;
        $this->cntl_semester = CTRL_SEMESTER;
        $this->cmd           = VARS::request("cmd");
        $this->tmp_key       = VARS::post("executedate");

        if($this->tmp_key != ""){
            $this->tmp_key = str_replace("/","-",$this->tmp_key);
            $this->cntl_dt_key = $this->tmp_key;
        }

        //セキュリティーチェック
        $this->staffcd = STAFFCD ;
        $this->sec_competence = AUTHORITY;
    }

    //入力項目チェック
    function validateInput()
    {
        //更新用日時
        if($this->cntl_dt_key == ""){
            $this->setWarning("MSG307");
            return false;
        }

        return true;
    }

    /**
     * 編集完了用のモデルを返します
    */
    function &getUpdateModel()
    {
        //入力チェック
        if ($this->validateInput()){
            if($this->update()){
                $this->setWarning("MSG201");
            }
        }
    }

    /**
     * アップデート時の内部メソッド
     */
    function &update()
    {
        //得点入力画面編集対象データの書込み処理
        $result = knjc030Query::getUpdateQuery($this);
        return $result;
    }

    //キー値を移動
    function &key_Move_Model($move_key)
    {
        $changed_day = knjc030Query::keyMoverQuery($this->cntl_dt_key,$move_key);

        if($changed_day != "")
        {
            $this->cntl_dt_key = $changed_day;
        }else{
            $this->setWarning("MSG303");
        }

    }

    //年度内の処理か？チェック
    function checkCtrlDay($day)
    {
        $day = str_replace("/","-",$day);

        //年度内の処理に限る
        if(($this->cntl_dt_year."-04-01" <= $day && $day < ($this->cntl_dt_year + 1)."-04-01"))
        {
            return true;
        }else{
            $this->setWarning("MSG915","(処理年度範囲外)");
            return false;
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

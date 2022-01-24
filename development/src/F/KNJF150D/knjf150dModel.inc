<?php

require_once('for_php7.php');

class knjf150dModel extends Model {
    var $cmd;
    var $cntl_dt_year;                      //学籍処理日の年(処理日が無ければウィンドウを閉じる)
    var $attnd_cntl_dt;                     //出力制御日付  (制御日付外は管理者のみ)
    var $cntl_dt_key;                       //コントロールデートキー
    var $executedate;                       //カレンダーコントロール用データ
    var $tmp_key;                           //カレンダーコントロール用データ一時保持
    //var $update_flg;                        //JavaScript"confirm関数"表示用フラグ
    var $staffcd;                           //スタッフコード
    var $check_staff_dat;                   //職員年度の職員データをチェック
    var $checked;                           //チェック選択

    #Take time 
    function getMicrotime(){
        list($usec, $sec) = explode(" ",microtime());
        return ((float)$sec + (float)$usec);
    }
    #end 

    function knjf150dModel()
    {
        //初期値取得
        $this->cntl_dt_key     = str_replace("/","-",CTRL_DATE);
        //$this->update_flg      = "";
        $this->checked         = array();
    }

    function init()
    {
        $this->attnd_cntl_dt   = ATTEND_CTRL_DATE;
        $this->staffcd         = STAFFCD;

        $this->cmd              = VARS::request("cmd");
        $this->tmp_key          = VARS::post("executedate");
        //$this->update_flg       = VARS::post("update_flg");

        if($this->tmp_key != ""){
            $this->tmp_key = str_replace("/","-",$this->tmp_key);
            $this->cntl_dt_key = $this->tmp_key;
        }

        //control year 初期化
        //処理年度(表示/更新時にも利用するため、Formと同じ処理を実装。年月日の変わるタイミングを考慮)
        if ($this->cntl_dt_key) {
            $cutarry = explode("-", $this->cntl_dt_key);
            $this->cntl_dt_year    = $cutarry[1] > 3 ? $cutarry[0] : ($cutarry[0] - 1);
        } else {
            $this->cntl_dt_year    = CTRL_YEAR;
        }
        //先生が登録されていない場合はブラウザを閉じる
        $this->check_staff_dat = (knjf150dQuery::getStaffCount($this))? "ok" : "ng" ;

        $this->staffcd = STAFFCD;
        $this->checked  = VARS::post("CHECKED");     //チェック

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //入力項目チェック
    function validateInput()
    {
        if (!isset($this->checked) || !is_array($this->checked) || get_count($this->checked) < 1) {
            return false;
        }
        return true;
    }

    function &getdeleteModel()
    {
        //入力チェック
        if ($this->validateInput()){
            if($this->delete()){
                $this->setMessage("MSG201");
            }
        } else {
            $this->setWarning("MSG308");
        }
        return;
    }

    function &delete()
    {
        //編集対象データの書込み処理
        $result = knjf150dQuery::getDeleteQuery($this);
        return $result;
    }

    //キー値を移動
    function &key_Move_Model($move_key)
    {
        $changed_day = knjf150dQuery::keyMoverQuery($this, $this->cntl_dt_key, $move_key);
        if($changed_day != "")
        {
            $this->cntl_dt_key = $changed_day;
        }else{
            $this->setWarning("MSG303");
        }

        return;
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
}
?>

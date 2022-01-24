<?php

require_once('for_php7.php');

class knjc020Model extends Model {
    var $cmd;
    var $cntl_dt_year;                      //学籍処理日の年(処理日が無ければウィンドウを閉じる)
    var $attnd_cntl_dt;                     //出力制御日付  (制御日付外は管理者のみ)
    var $cntl_dt_key;                       //コントロールデートキー
    var $executedate;                       //カレンダーコントロール用データ
    var $tmp_key;                           //カレンダーコントロール用データ一時保持
    var $chosen_id;                         //更新用データ保持
    var $stock_chaircd;                     //チェアーコード保持
    var $update_flg;                        //JavaScript"confirm関数"表示用フラグ
    var $first_id;                          //初期保持データ
    var $first_time_flg;                    //JavaScript初回選択フラグ
    var $color_bk;                          //選択データ保持変数
    var $staffcd;                           //スタッフコード
    var $set_target;                        //変更を加えた場所に移動するためのフラグ
    var $check_staff_dat;                   //職員年度の職員データをチェック
    var $SUBJECT;                           //選択中の科目
    var $DispCol;                           //表示項目


    #Take time 
    function getMicrotime(){
        list($usec, $sec) = explode(" ",microtime());
        return ((float)$sec + (float)$usec);
    }
    #end 

    function knjc020Model()
    {
        //初期値取得
        $this->cntl_dt_key     = str_replace("/","-",CTRL_DATE);
        $this->update_flg      = "";
        $this->chosen_id       = "";
        $this->first_id        = "";
        $this->color_bk        = "";
        $this->first_time_flg  = "off" ;
        $this->set_target      = "off" ;
    }

    function init()
    {
        //先生が登録されていない場合はブラウザを閉じる
        $this->check_staff_dat = (knjc020Query::getStaffCount())? "ok" : "ng" ;

        //control year 初期化
        $this->cntl_dt_year    = CTRL_YEAR;
        $this->attnd_cntl_dt   = ATTEND_CTRL_DATE;
        $this->staffcd         = STAFFCD;

        $this->cmd              = VARS::request("cmd");
        $this->tmp_key          = VARS::post("executedate");
        $this->chosen_id        = VARS::post("chosen_id");
        $this->stock_chaircd    = VARS::post("stock_chaircd");
        $this->update_flg       = VARS::post("update_flg");
        $this->first_id         = VARS::post("ID_NO");
        $this->first_time_flg   = VARS::post("locker");
        $this->color_bk         = VARS::post("backupper");
        $this->SUBJECT          = VARS::post("sub_combo");

        if($this->tmp_key != ""){
            $this->tmp_key = str_replace("/","-",$this->tmp_key);

            //カレンダーコントロールからの日時をチェック
            if ($this->tmp_key != $this->cntl_dt_key) $this->keyClean();
            $this->cntl_dt_key = $this->tmp_key;
        }

        //セキュリティーチェック
        $this->staffcd = STAFFCD;

        $this->getPropertiesAll(); //プロパティファイルの読込み
    }

    //入力項目チェック
    function validateInput()
    {
        //更新用データチェック
        if($this->chosen_id == ""){
            $this->setWarning("MSG301");
            return false;
        }

        //更新用日時
        if($this->cntl_dt_key == ""){
            $this->setWarning("MSG307");
            return false;
        }

        //出力制御日付後のデータ変更不可
        if (!IS_KANRISYA && $this->cntl_dt_key <= $this->attnd_cntl_dt) {
            $this->setWarning("MSG300","（ 出力制御日付: ".$this->attnd_cntl_dt."以前のデータ ）");
            return false;
        }

        if ($this->cmd == "update"){
            //アップデート確認用処理
            switch ($this->update_flg) {
                case "":
                    if(knjc020Query::chk_attenddata($this)){
                        $this->update_flg = "on";
                        return false;
                    }else{
                        break;
                    }
                case "on":
                    $this->update_flg = "";
                    return false;
                case "off":
                    $this->update_flg = "";
                    //データを削除
                    $result = knjc020Query::delete_attenddata($this);
            }
        }
        return true;
    }

    function &getUpdateModel()
    {

        //入力チェック
        if ($this->validateInput()){
            if($this->update()){
                $ary = explode(",",$this->chosen_id);
                $this->set_target = $ary[1];
                $this->setMessage("MSG201");
            }
        }
        return;
    }

    function &update()
    {
        //編集対象データの書込み処理
        $result = knjc020Query::getUpdateQuery($this, $this->termIs);
        return $result;
    }

    //キー値を移動
    function &key_Move_Model($move_key)
    {
        $changed_day = knjc020Query::keyMoverQuery($this->cntl_dt_key,$move_key);

        if($changed_day != "")
        {
            //年度内の処理に限る
            if($this->checkCtrlDay($changed_day))
            {   
                $this->cntl_dt_key = $changed_day;
                $this->first_id_bk = $this->first_id;
                $this->keyClean();
            }
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

    //更新用データを消去
    function keyClean()
    {
        $this->first_id   = "";
        $this->chosen_id  = "";
        return;
    }
}
?>

<?php

require_once('for_php7.php');

class knjc201_absence_listModel extends Model
{
    public $cmd;
    public $cntl_dt_year;                      //学籍処理日の年(処理日が無ければウィンドウを閉じる)
    public $attnd_cntl_dt;                     //出力制御日付  (制御日付外は管理者のみ)
    public $cntl_dt_key;                       //コントロールデートキー
    public $executedate;                       //カレンダーコントロール用データ
    public $tmp_key;                           //カレンダーコントロール用データ一時保持
    public $first_id;                          //初期保持データ
    public $color_bk;                          //選択データ保持変数
    public $DispCol;
    public $instate;

    public function knjc201_absence_listModel()
    {
        //初期値取得
        $this->cntl_dt_key     = str_replace("/", "-", CTRL_DATE);
        $this->first_id        = "";
        $this->color_bk        = "";
    }

    public function init()
    {
        $this->cntl_dt_year    = CTRL_YEAR;
        $this->attnd_cntl_dt   = ATTEND_CTRL_DATE;

        $this->cmd              = VARS::request("cmd");
        $this->tmp_key          = VARS::post("executedate");
        $this->first_id         = VARS::post("ID_NO");
        $this->color_bk         = VARS::post("backupper");
        $this->target_check     = VARS::post("TARGET_CHECK");

        $this->GRADE            = VARS::post("GRADE");
        $this->CLASS            = VARS::post("CLASS");

        if (is_array($this->target_check)) {
            $sep = "";
            $this->instate = "(";
            foreach ($this->target_check as $key => $val) {
                $this->instate .= $sep."'".$val."'";
                $sep = ",";
            }
            $this->instate .= ")";
        } else {
            $this->instate = "('')";
        }

        if ($this->tmp_key != "") {
            $this->tmp_key = str_replace("/", "-", $this->tmp_key);

            //カレンダーコントロールからの日時をチェック
            if ($this->tmp_key != $this->cntl_dt_key) {
                $this->keyClean();
            }
            $this->cntl_dt_key = $this->tmp_key;
        }

        $this->getPropertiesAll(); //プロパティファイルの読込み
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //キー値を移動(データの検索)
    public function keyMoveModel($move_key)
    {
        switch ($move_key) {
            case 'next':
                $changed_day = $this->moveData($this->cntl_dt_key, 1);
                break;
            case 'before':
                $changed_day = $this->moveData($this->cntl_dt_key, -1);
                break;
            case 'changeDate':
                $changed_day = $this->moveData($this->cntl_dt_key, 0);//ボタンではなく、直接入力で日付が変更された場合
                break;
            default:
                //ここに来ることはありえない
                return;
        }
        if ($changed_day != "") {
            //年度内の処理に限る
            if ($this->checkCtrlDay($changed_day)) {
                $this->cntl_dt_key = $changed_day;
                $this->first_id_bk = $this->first_id;
                $this->first_id;
                $this->keyClean();
            }
        } else {
            $this->setWarning("MSG303");
        }
        return;
    }

    //第1引数 : "yyyy-mm-dd"
    //第2引数 : "日付の差の日数(整数値)"
    public function moveData($curDay, $diff)
    {
        return date('Y-m-d', strtotime("{$curDay} {$diff} day"));
    }

    //年度内の処理か？チェック
    public function checkCtrlDay($day)
    {
        //年度内の処理に限る
        if (($this->cntl_dt_year."-04-01" <= $day && $day < ((int)$this->cntl_dt_year + 1)."-04-01")) {
            return true;
        } else {
            $this->setWarning("MSG915", "(処理年度範囲外)");
            return false;
        }
    }

    //更新用データを消去
    public function keyClean()
    {
        $this->first_id = "";
        return;
    }
}

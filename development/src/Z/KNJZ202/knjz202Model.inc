<?php

require_once('for_php7.php');


class knjz202Model extends Model {
    
    var $cmd;
    var $year;
    var $date;
    var $control = array();
    var $month;
    var $allMonth = array();
    var $virus;
    var $koudome;

    function knjz202Model()
    {
    }

    function init()
    {  
        common::GetControlMaster_Fnc($this->control);
        $this->year     = VARS::post("year");
        $this->date     = str_replace("/","-",VARS::post("date"));
        $this->cmd      = VARS::request("cmd");
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        $this->field = array("ABSENCE_WARN_CHECK"  =>  VARS::post("ABSENCE_WARN_CHECK"),
                             "ABSENCE_WARN_CHECK2" =>  VARS::post("ABSENCE_WARN_CHECK2"),
                             "ABSENCE_WARN_CHECK3" =>  VARS::post("ABSENCE_WARN_CHECK3"),
                             "ABSENCE_WARN"        =>  VARS::post("ABSENCE_WARN"),
                             "ABSENCE_WARN2"       =>  VARS::post("ABSENCE_WARN2"),
                             "ABSENCE_WARN3"       =>  VARS::post("ABSENCE_WARN3")
                             );
        //初期値の設定
        $this->virus = ($this->Properties["useVirus"] == "true") ? $this->Properties["useVirus"] : false;
        $this->koudome = ($this->Properties["useKoudome"] == "true") ? $this->Properties["useKoudome"] : false;
    }

    //入力項目チェック
    function validateInput()
    {
        //日付の年度内チェック
        $rtnflg = knjz202Query::getCheckDate($this);
        if ($rtnflg) {
            $this->setWarning("MSG901", "\\n日付は、年度内を指定して下さい。");
            return false;
        }

        //日付の学期・月を取得
        $this->setMonth();

        return true;
    }

    //更新
    function &getUpdateModel() {
        //入力チェック
        if ($this->validateInput()) {
            if (knjz202Query::getUpdateQuery($this)) {
                //更新(欠課数オーバー)
                if (($this->field["ABSENCE_WARN_CHECK"]  == "on" && strlen($this->field["ABSENCE_WARN"]))  || 
                    ($this->field["ABSENCE_WARN_CHECK2"] == "on" && strlen($this->field["ABSENCE_WARN2"])) || 
                    ($this->field["ABSENCE_WARN_CHECK3"] == "on" && strlen($this->field["ABSENCE_WARN3"])))
                {
                    knjz202Query::executeQuery($this);
                    knjz202Query::executeQuerySpecial($this);
                }
                $this->setMessage("MSG201");
            }
        }
    }

    //処理月（各学期の期間の月のみ）
    function setMonth() {
        //DB接続
        $db = Query::dbCheckOut();

        $this->month = "";
        $this->allMonth = array();
        $result = $db->query(knjz202Query::getSemesterMonth($this->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            for ($i = 4; $i < 16; $i++) 
            {
                $mon = ($i<13) ? $i : ($i-12);

                if ($mon < 4) {
                    $year = (int)$this->year + 1;
                } else {
                    $year = $this->year;
                }

                //年と月を合わせて比較する
                if ((int)($year.sprintf("%02d",$mon)) >= (int)strftime("%Y%m",strtotime($row["SDATE"])) 
                && ((int)$year.sprintf("%02d",$mon)) <= (int)strftime("%Y%m",strtotime($row["EDATE"]))) 
                {
                    /*** 月が学期の開始月または終了月かチェック ***/
                    //開始月の場合は開始日以降その月末日まで集計
                    if ($mon == (int)strftime("%m",strtotime($row["SDATE"]))) {

                        $flg = "1";

                    //終了月の場合はその月の１日から終了日まで集計
                    } else if ($mon == (int)strftime("%m",strtotime($row["EDATE"]))) {

                        $flg = "2";

                    //それ以外はその月の１日から月末日まで集計
                    } else {

                        $flg = "0";
                    }

                    //日付の月
                    if ($this->month == "") {
                        if ($mon == strftime("%m", strtotime($this->date)) && $this->date <= $row["EDATE"]) {
                            $this->month = $row["SEMESTER"]."-".sprintf("%02d",$mon)."-".$flg;
//echo "date=" .$this->date .", month=" .$this->month;
                        }
                    }

                    //全ての学期・月
                    $this->allMonth[] = $row["SEMESTER"]."-".sprintf("%02d",$mon)."-".$flg;
                }
            }
        }

        //DB切断
        Query::dbCheckIn($db);
    }
}
?>

<?php
class knjd326kModel extends Model {

    var $cmd;
    var $schregno;                  //生徒学籍番号
    var $name;                      //生徒名
    var $GradeClass;                //学年、クラス
    var $control_data = array();    //コントロールマスタデータ
    var $replace_data = array();    //一括更新処理データ
    var $cntl_dt_year;              //処理年度(CTRL_YEAR)
    var $cntl_last_year;            //処理年度(CTRL_YEAR-1)
    var $cntl_beforelast_year;      //処理年度(CTRL_YEAR-2)
    var $cntl_semester;             //処理学期(CTRL_SEMESTER)
    var $flg;                       //前へ次への押下判定用フラグ
    var $num;                       //表示画面番号
    var $gamensu;                   //トータル画面数
    var $nendo_flg;                 //年度別データフラグ
    var $last_grad;                 //前学年
    var $last_hrcl;                 //前クラス
    var $last_atno;                 //前番号
    var $bfor_grad;                 //前々学年
    var $bfor_hrcl;                 //前々クラス
    var $bfor_atno;                 //前々番号
    var $first    ;                 //拡大判定用
    var $firstcnt ;                 //拡大判定用
    var $numkaku ;                  //拡大判定用

    function knjd326kModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
        unset($this->first);
        unset($this->firstcnt);
    }

    function init()
    {
        $this->cmd = VARS::request("cmd");
        if ($this->cmd == 'kakuend') {
            $this->cmd = 'main' ;
            $this->num = $this->numkaku ;
            $this->firstcnt  = 0;
        }
        if ($this->cmd == 'kakudai') {
            $this->firstcnt  = 0;
        }
        if ($this->cmd == 'kakudai' && VARS::get("FLG") != 0 && !$this->firstcnt) {
                if (VARS::get("NAME")) {
                    $this->name = VARS::get("NAME");
                }
                $this->first     = 0;
                $this->firstcnt  = 0;
                $this->schregno  = VARS::get("SCHREGNO");
                $this->num       = VARS::get("num");
                $this->gamensu   = VARS::get("gamensu");
                $this->nendo_flg = VARS::get("nendo_flg");
                $this->last_grad = VARS::get("last_grad");
                $this->last_hrcl = VARS::get("last_hrcl");
                $this->last_atno = VARS::get("last_atno");
                $this->bfor_grad = VARS::get("bfor_grad");
                $this->bfor_hrcl = VARS::get("bfor_hrcl");
                $this->bfor_atno = VARS::get("bfor_atno");
        } else {
                //起動時に初期化
                if (VARS::get("PROGRAMID")) {
                    unset($this->schregno);
                    unset($this->name);
                    unset($this->GradeClass);
                    $this->num = 0;
                    $this->gamensu = 0;
                    $this->nendo_flg = 0;
                }
                //リストの年組コンボが変更されたとき
                if (VARS::get("init")) {
                    unset($this->schregno);
                    unset($this->name);
                    unset($this->GradeClass);
                    $this->num = 0;
                    $this->gamensu = 0;
                    $this->nendo_flg = 0;
                    $this->last_grad = "";
                    $this->last_hrcl = "";
                    $this->last_atno = "";
                    $this->bfor_grad = "";
                    $this->bfor_hrcl = "";
                    $this->bfor_atno = "";
                }
                if (VARS::get("SCHREGNO")) {
                    if ($this->schregno != VARS::get("SCHREGNO")) {
                        $this->last_grad = "";
                        $this->last_hrcl = "";
                        $this->last_atno = "";
                        $this->bfor_grad = "";
                        $this->bfor_hrcl = "";
                        $this->bfor_atno = "";
                    }
                    $this->num = 0;
                    $this->flg = 0;
                    $this->gamensu =0;
                    $this->nendo_flg =0;
                    $this->schregno  = VARS::get("SCHREGNO");
                }
                if (VARS::get("NAME")) {
                    $this->name = VARS::get("NAME");
                }

                $this->cntl_dt_year         = CTRL_YEAR;
                $this->cntl_last_year       = (CTRL_YEAR - 1);
                $this->cntl_beforelast_year = (CTRL_YEAR - 2);

                $this->cntl_semester = CTRL_SEMESTER;
                if (VARS::get("FLG")) {
                    $this->flg = VARS::get("FLG");
                } else {
                    $this->flg = 0;
                }
                if (VARS::request("num")) {
                    $this->num = VARS::request("num");
                }
                if (VARS::request("gamensu")) {
                    $this->gamensu = VARS::request("gamensu");
                }
                if (VARS::request("nendo_flg")) {
                    $this->nendo_flg = VARS::request("nendo_flg");
                }
                if (VARS::request("last_grad")) {
                    $this->last_grad = VARS::request("last_grad");
                }
                if (VARS::request("last_hrcl")) {
                    $this->last_hrcl = VARS::request("last_hrcl");
                }
                if (VARS::request("last_atno")) {
                    $this->last_atno = VARS::request("last_atno");
                }
                if (VARS::request("bfor_grad")) {
                    $this->bfor_grad = VARS::request("bfor_grad");
                }
                if (VARS::request("bfor_hrcl")) {
                    $this->bfor_hrcl = VARS::request("bfor_hrcl");
                }
                if (VARS::request("bfor_atno")) {
                    $this->bfor_atno = VARS::request("bfor_atno");
                }

        }
        if (VARS::get("ini2")) {
            unset($this->replace_data);
        }
    }

    //変数をクリアします
    function clean()
    {
    }
}
?>
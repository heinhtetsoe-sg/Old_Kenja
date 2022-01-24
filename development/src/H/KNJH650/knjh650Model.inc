<?php

require_once('for_php7.php');
class knjh650Model extends Model
{
    public $cmd;
    public $schregno = "";
    public $name = "";
    public $grade;
    public $hr_class;
    public $field = array();
    public $gakki;
    public $getPrgId;  //コールされたか
    public $sendAuth;  //親画面の権限
    public $auth;      //権限

    public function knjh650Model()
    {
    }

    public function init()
    {
        if (VARS::get("PROGRAMID")) {
            unset($this->getPrgId);
            unset($this->sendAuth);
            unset($this->auth);
        }
        if (VARS::get("SEND_PRGID")) {
            $this->getPrgId = VARS::get("SEND_PRGID");
            $this->sendAuth = VARS::get("SEND_AUTH");
        }
        $this->auth = $this->getPrgId ? $this->sendAuth : AUTHORITY;

        if (VARS::get("PROGRAMID")!="") {
            unset($this->schregno);
        }
        if (VARS::get("PROGRAMID")!="") {
            unset($this->name);
            unset($this->gakki);
        }
        $this->field = array("BUNRIDIV"         =>  VARS::post("BUNRIDIV"),
                             "CENTER_ABSENT"    =>  VARS::post("CENTER_ABSENT"),
                             "MARK_ABSENT"      =>  VARS::post("MARK_ABSENT")
                             );

        $this->cmd = VARS::request("cmd");
        //プロパティファイル
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SCHREGNO")!="") {
            $this->schregno   = VARS::get("SCHREGNO");
        }
        if (VARS::get("NAME")!="") {
            $this->name       = VARS::get("NAME");
        }

        $this->grade = VARS::get("GRADE");
        $this->hr_class = VARS::get("HR_CLASS");
        //リストの年組コンボが変更されたとき
        if (VARS::get("init")) {
            unset($this->schregno);
            unset($this->name);
            unset($this->gakki);
        }
        if (VARS::post("GAKKI")!="") {
            $this->gakki = VARS::post("GAKKI");
        }
    }

    public function &getProgrampathModel()
    {
        $db = Query::dbCheckOut();
        $programpath = $db->getOne(knjh650Query::getProgrampath());
        Query::dbCheckIn($db);

        return $programpath;
    }

    //入力項目チェック
    public function validateInput()
    {
        return true;
    }

    public function &getUpdateModel()
    {
        if (!$this->validateInput()) {
            return;
        }
        $this->update();
        $this->setMessage("MSG201");
        return;
    }

    public function &update()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $this->totalScore = array();
        $englishCnt = 0;
        $totalScore = 0;
        $totalPerfect = 0;
        $kansan800 = 0;
        $kansan800Perfect = 0;
        $kansan500Bun = 0;
        $kansan500BunPerfect = 0;
        $kansan500Ri1 = 0;
        $kansan500Ri2 = 0;
        $kansan500RiPerfect = 0;
        $kansan900Bun1 = 0;
        $kansan900Bun2 = 0;
        $kansan900Ri1 = 0;
        $kansan900Ri2 = 0;
        
        foreach ($this->subclassCdArray as $key => $val) {
            $this->updData[$key]["CLASSCD"] = $val["CENTER_CLASS_CD"];
            $this->updData[$key]["SCORE"] = VARS::post("SCORE_{$key}");
            $this->updData[$key]["DAIITI_SENTAKU"] = VARS::post("CHECK_{$key}");
            if (strlen($this->updData[$key]["SCORE"]) > 0) {
                $this->totalScore[$val["CENTER_CLASS_CD"]]["SCORE"] += (int)$this->updData[$key]["SCORE"];
                $this->totalScore[$val["CENTER_CLASS_CD"]]["CNT"]++;
                if ($val["CENTER_CLASS_CD"] == "01") {
                    $englishCnt++;
                }
            }

            if (strlen($this->updData[$key]["SCORE"]) > 0) {
                $totalPerfect += (int)$val["ALLOT_POINT"];
            }
            //社会OR理科２
            if ($val["CENTER_CLASS_CD"] == "04" || $val["CENTER_CLASS_CD"] == "06") {
                if (strLen($this->updData[$key]["SCORE"]) > 0) {
                    if ($this->updData[$key]["DAIITI_SENTAKU"] == "1") {
                        $kansan800     += (int)$this->updData[$key]["SCORE"];
                        $kansan500Bun  += $val["BUNRIDIV"] == "1" ? (int)$this->updData[$key]["SCORE"] : 0;
                        $kansan500Ri2  += $val["BUNRIDIV"] == "2" ? (int)$this->updData[$key]["SCORE"] : 0;
                        $kansan800Perfect    += strlen($this->updData[$key]["SCORE"]) > 0 ? (int)$val["ALLOT_POINT"] : 0;
                        $kansan500BunPerfect += $val["BUNRIDIV"] == "1" && strlen($this->updData[$key]["SCORE"]) > 0 ? (int)$val["ALLOT_POINT"] : 0;
                        $kansan500RiPerfect  += $val["BUNRIDIV"] == "2" && strlen($this->updData[$key]["SCORE"]) > 0 ? (int)$val["ALLOT_POINT"] : 0;
                    } else {
                        $kansan800Perfect    += strlen($this->updData[$key]["SCORE"]) > 0 ? (int)$val["ALLOT_POINT"] : 0;
                    }
                    if ($val["CENTER_CLASS_CD"] == "04") {
                        $kansan900Bun1 += (int)$this->updData[$key]["SCORE"];
                        $kansan900Bun2 += (int)$this->updData[$key]["SCORE"];
                        $kansan900Ri2  += $this->updData[$key]["DAIITI_SENTAKU"] == "1" ? (int)$this->updData[$key]["SCORE"] : 0;
                        $kansan900Ri1  += $this->updData[$key]["DAIITI_SENTAKU"] == "1" ? (int)$this->updData[$key]["SCORE"] : 0;
                    } else {
                        $kansan900Ri2  += $this->updData[$key]["SCORE"];
                        $kansan900Bun1 += $this->updData[$key]["DAIITI_SENTAKU"] == "1" ? (int)$this->updData[$key]["SCORE"] : 0;
                        $kansan900Bun2 += $this->updData[$key]["DAIITI_SENTAKU"] == "1" ? (int)$this->updData[$key]["SCORE"] : 0;
                    }
                }
            } elseif ($val["BUNRIDIV"] == "1") {
                if (strlen($this->updData[$key]["SCORE"]) > 0) {
                    $kansan800     += (int)$this->updData[$key]["SCORE"];
                    $kansan500Bun  += (int)$this->updData[$key]["SCORE"];
                    $kansan900Bun1 += (int)$this->updData[$key]["SCORE"];
                    $kansan900Bun2 += (int)$this->updData[$key]["SCORE"];
                    $kansan900Ri1  += (int)$this->updData[$key]["SCORE"];
                    $kansan900Ri2  += (int)$this->updData[$key]["SCORE"];
                    $kansan800Perfect     += (int)$val["ALLOT_POINT"];
                    $kansan500BunPerfect  += (int)$val["ALLOT_POINT"];
                }
            } elseif ($val["BUNRIDIV"] == "2") {
                if (strlen($this->updData[$key]["SCORE"]) > 0) {
                    $kansan800     += (int)$this->updData[$key]["SCORE"];
                    $kansan500Ri1  += (int)$this->updData[$key]["SCORE"];
                    $kansan500Ri2  += $val["CENTER_CLASS_CD"] == "05" ? 0 : (int)$this->updData[$key]["SCORE"];
                    $kansan900Bun1 += (int)$this->updData[$key]["SCORE"];
                    $kansan900Bun2 += $val["CENTER_CLASS_CD"] == "05" ? 0 : (int)$this->updData[$key]["SCORE"];
                    $kansan900Ri1  += (int)$this->updData[$key]["SCORE"];
                    $kansan900Ri2  += $val["CENTER_CLASS_CD"] == "05" ? 0 : (int)$this->updData[$key]["SCORE"];
                    $kansan800Perfect    += (int)$val["ALLOT_POINT"];
                    $kansan500RiPerfect  += (int)$val["ALLOT_POINT"];
                }
            } else {
                if (strlen($this->updData[$key]["SCORE"]) > 0) {
                    $kansan800    += (int)$this->updData[$key]["SCORE"];
                    $kansan500Bun += (int)$this->updData[$key]["SCORE"];
                    $kansan500Ri1  += (int)$this->updData[$key]["SCORE"];
                    $kansan500Ri2  += (int)$this->updData[$key]["SCORE"];
                    $kansan900Bun1 += (int)$this->updData[$key]["SCORE"];
                    $kansan900Bun2 += (int)$this->updData[$key]["SCORE"];
                    $kansan900Ri1  += (int)$this->updData[$key]["SCORE"];
                    $kansan900Ri2  += (int)$this->updData[$key]["SCORE"];
                    $kansan800Perfect    += (int)$val["ALLOT_POINT"];
                    $kansan500BunPerfect += (int)$val["ALLOT_POINT"];
                    $kansan500RiPerfect  += (int)$val["ALLOT_POINT"];
                }
            }
            if (strlen($this->updData[$key]["SCORE"]) > 0) {
                $totalScore += (int)$this->updData[$key]["SCORE"];
            }
        }

        //英語
        if (strLen($this->totalScore["01"]["SCORE"]) > 0) {
            $kansan800     = $kansan800 - (int)$this->totalScore["01"]["SCORE"] + (int)$this->totalScore["01"]["SCORE"] * 0.8;
            $kansan500Bun  = $kansan500Bun - (int)$this->totalScore["01"]["SCORE"] + (int)$this->totalScore["01"]["SCORE"] * 0.8;
            $kansan500Ri1  = $kansan500Ri1 - (int)$this->totalScore["01"]["SCORE"] + (int)$this->totalScore["01"]["SCORE"] * 0.8;
            $kansan500Ri2  = $kansan500Ri2 - (int)$this->totalScore["01"]["SCORE"] + (int)$this->totalScore["01"]["SCORE"] * 0.8;
            $kansan900Bun1 = $kansan900Bun1 - (int)$this->totalScore["01"]["SCORE"] + (int)$this->totalScore["01"]["SCORE"] * 0.8;
            $kansan900Bun2 = $kansan900Bun2 - (int)$this->totalScore["01"]["SCORE"] + (int)$this->totalScore["01"]["SCORE"] * 0.8;
            $kansan900Ri1  = $kansan900Ri1 - (int)$this->totalScore["01"]["SCORE"] + (int)$this->totalScore["01"]["SCORE"] * 0.8;
            $kansan900Ri2  = $kansan900Ri2 - (int)$this->totalScore["01"]["SCORE"] + (int)$this->totalScore["01"]["SCORE"] * 0.8;
            $totalScore    = $totalScore - (int)$this->totalScore["01"]["SCORE"] + (int)$this->totalScore["01"]["SCORE"] * 0.8;
            $this->totalScore["01"]["SCORE"] = (int)$this->totalScore["01"]["SCORE"] * 1.0;
        }

        knjh650Query::getDeleteDatQuery($db, $this);
        knjh650Query::getInsertDatQuery($db, $this);

        knjh650Query::getDeleteRankQuery($db, $this);
        knjh650Query::getInsertRankQuery($db, $this);

        knjh650Query::getDeleteTotalScoreQuery($db, $this);
        knjh650Query::getInsertTotalScoreQuery($db, $this);

        knjh650Query::getDeleteKansanQuery($db, $this);

        knjh650Query::getInsertKansanQuery($db, $this, "02", "1", $kansan500Bun);

        if ($kansan500RiPerfect <= 500) {
            $kansan500Ri = $kansan500Ri1 > $kansan500Ri2 ? $kansan500Ri1 : $kansan500Ri2;
            knjh650Query::getInsertKansanQuery($db, $this, "02", "2", $kansan500Ri);
        } else {
            $kansan500Ri = $kansan500Ri2;
            knjh650Query::getInsertKansanQuery($db, $this, "02", "2", $kansan500Ri);
        }

        if ($kansan800Perfect <= 800) {
            $kansan800 = $totalScore;
            if ($this->field["BUNRIDIV"] == "1" && $kansan800 != $kansan500Bun ||
                $this->field["BUNRIDIV"] == "2" && $kansan800 != $kansan500Ri
                ) {
                knjh650Query::getInsertKansanQuery($db, $this, "01", "0", $kansan800);
            }
        } else {
            if ($this->field["BUNRIDIV"] == "1" && $kansan800 != $kansan500Bun ||
                $this->field["BUNRIDIV"] == "2" && $kansan800 != $kansan500Ri
                ) {
                knjh650Query::getInsertKansanQuery($db, $this, "01", "0", $kansan800);
            }
        }

        $kansan900Bun += $kansan900Bun1 > $kansan900Bun2 ? $kansan900Bun1 : $kansan900Bun2;
        if ($this->field["BUNRIDIV"] == "1" && $kansan900Bun != $kansan500Bun ||
            $this->field["BUNRIDIV"] == "2" && $kansan900Bun != $kansan500Ri
            ) {
            knjh650Query::getInsertKansanQuery($db, $this, "03", "1", $kansan900Bun);
        }

        $kansan900Ri += $kansan900Ri1 > $kansan900Ri2 ? $kansan900Ri1 : $kansan900Ri2;
        if ($this->field["BUNRIDIV"] == "1" && $kansan900Ri != $kansan500Bun ||
            $this->field["BUNRIDIV"] == "2" && $kansan900Ri != $kansan500Ri
            ) {
            knjh650Query::getInsertKansanQuery($db, $this, "03", "2", $kansan900Ri);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}

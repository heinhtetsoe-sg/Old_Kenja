<?php

require_once('for_php7.php');

class knja350Model extends Model {

    var $cmd;
    var $last_year;
    var $this_year;
    var $next_year;
    var $staffcd;
    var $sec_competence;

    function knja350Model() {
    }

    function init() {
        $this->field = array("PROC"       => VARS::post("PROC"),      // 第N回目
                             );

        $this->last_year        = CTRL_YEAR - 1;                    //昨年度
        $this->this_year        = CTRL_YEAR;                        //現在年度
        $this->next_year        = CTRL_YEAR + 1;                    //設定年度

        $this->school_kind = VARS::request("SCHOOL_KIND");

        $this->cmd = VARS::request("cmd");

        //セキュリティーチェック
        $this->staffcd = STAFFCD;
        $this->sec_competence = common::SecurityCheck(STAFFCD, PROGRAMID);

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }
    }

    //実行
    function &getUpdateModel() {
        if ($this->update()) {
            $this->setMessage("MSG201");
        } else {
            $this->setMessage("", "処理に失敗しました。");
        }
    }

    //更新
    function update() {
        $tabs = [];
        if ($this->field["PROC"] == "1") {
            //第1回目
            $tabs = [
                  ["tabname" => "SCHREG_REGD_HDAT"       ]
                , ["tabname" => "STAFF_CLASS_HIST_DAT"   ]
                , ["tabname" => "SCHREG_BASE_MST"        ]
                , ["tabname" => "CLASS_FORMATION_DAT"    ] // ADD
                , ["tabname" => "SCHREG_REGD_DAT"        , "cols" => ["SCHREGNO", "YEAR", "SEMESTER", "GRADE", "HR_CLASS", "ATTENDNO", "COURSECD", "MAJORCD", "COURSECODE", "REGISTERCD", "UPDATED"]]
                , ["tabname" => "SCHREG_BASE_DETAIL_MST" ]
                , ["tabname" => "SCHREG_ENT_GRD_HIST_DAT"]
                , ["tabname" => "SCHREG_BASE_HIST_DAT"   ]
                , ["tabname" => "SCHREG_ADDRESS_DAT"     ]
                , ["tabname" => "GUARDIAN_DAT"           ]
                , ["tabname" => "GUARDIAN_ADDRESS_DAT"   ]
                , ["tabname" => "GUARDIAN_HIST_DAT"      ]
                , ["tabname" => "GUARDIAN2_DAT"          ]
                , ["tabname" => "GUARDIAN2_ADDRESS_DAT"  ]
                , ["tabname" => "GUARDIAN2_HIST_DAT"     ]
                , ["tabname" => "GUARANTOR_ADDRESS_DAT"  ]
                , ["tabname" => "GUARANTOR_HIST_DAT"     ]
                , ["tabname" => "SCHREG_SEND_ADDRESS_DAT"]
                , ["tabname" => "SCHREG_RELA_DAT"        ]
                , ["tabname" => "SCHREG_TRANSFER_DAT"    ]
                , ["tabname" => "SCHREG_ENVIR_DAT"       ]
                , ["tabname" => "STAFF_MST"              ]
                , ["tabname" => "STAFF_YDAT"             ]
                , ["tabname" => "STAFF_DETAIL_MST"       ]
                , ["tabname" => "STAFF_NAME_HIST_DAT"    ]
                , ["tabname" => "STAFF_ADDRESS_DAT"      ]
            ];
        } else if ($this->field["PROC"] == "2") {
            //第2回目
            $tabs = [
                  ["tabname" => "SCHREG_STUDYREC_DAT"         ]
                , ["tabname" => "STUDYRECREMARK_DAT"          ]
                , ["tabname" => "JVIEWSTAT_SUB_DAT"           ]
                , ["tabname" => "SCHREG_ATTENDREC_DAT"        ]
                , ["tabname" => "ATTEND_SEMES_DAT"            ]
                , ["tabname" => "ATTEND_SEMES_REMARK_DAT"     ]
                , ["tabname" => "HTRAINREMARK_DAT"            ]
                , ["tabname" => "BEHAVIOR_DAT"                ]
                , ["tabname" => "HTRAINREMARK_DETAIL2_DAT"    ]
                , ["tabname" => "SCHREG_CLUB_HIST_DAT"        ]
                , ["tabname" => "SCHREG_CLUB_HDETAIL_DAT"     ]
                , ["tabname" => "SCHREG_COMMITTEE_HIST_DAT"   ]
                , ["tabname" => "SCHREG_COMMITTEE_HDETAIL_DAT"]
                , ["tabname" => "SCHREG_QUALIFIED_HOBBY_DAT"  ]
                , ["tabname" => "MEDEXAM_HDAT"                ]
                , ["tabname" => "MEDEXAM_DET_DAT"             ]
                , ["tabname" => "MEDEXAM_DET_DETAIL_DAT"      ]
                , ["tabname" => "MEDEXAM_TOOTH_DAT"           ]
                , ["tabname" => "MEDEXAM_TOOTH_DETAIL_DAT"    ]
                , ["tabname" => "RECORD_SCORE_DAT"            ]
            ];
        }
        $debug = false;
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($debug) {
            foreach ($tabs as $tab) {
                $query = knja350Query::getSql($db, $this, $tab["tabname"], ["cols" => ["COUNT(*)"], "collision" => true]);
                $c = $db->getOne($query);
                if ($c > 0) {
                    $d = "";
                    $d .= "<pre>debug\n";
                    $d .= "重複数 ".$c."\n";
                    $d .= "table ". $tab["tabname"]."\n";
                    $d .= "query ".$query."\n";
                    $d .= "</pre>";
                    $d .= "<hr>";
                    echo $d;
                }
            }
        }

        $ret = true;
        $updateCount = 0;
        foreach ($tabs as $tab) {
            $opt = [];
            $opt["noCollision"] = true;
            if ($tab["cols"]) {
                $opt["cols"] = $tab["cols"];
            }
            $query = knja350Query::getInsertIntoSql($db, $this, $tab["tabname"], $opt);
            $db->query($query);
            $count = $db->affectedRows();
            $updateCount += (int)$count;
            if ($debug) {
                $d = "";
                $d .= "<pre>debug\n";
                $d .= "更新数 ".$count."\n";
                $d .= "table: ".$tab["tabname"]." \n";
                $d .= "query : ".$query."\n";
                $d .= "</pre>";
                $d .= "<hr>";
                echo $d;
            }
        }

        // if($updateCount == 0) {
        if ($ret == false) {
            $db->rollback();
        } else {
            $db->commit();
        }

        Query::dbCheckIn($db);
        return $ret;
    }

}
?>

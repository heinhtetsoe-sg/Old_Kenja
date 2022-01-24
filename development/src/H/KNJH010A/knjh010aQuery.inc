<?php

require_once('for_php7.php');
class knjh010aQuery extends Query
{
    //生徒環境調査データ取得
    public function getSchregEnvirDat($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENVIR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //生徒環境調査データ取得(subfrm)
    public function getSchregsubEnvirDat($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENVIR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->sch}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //名称マスタから名称取得
    public function getVNameMst($namecd, $model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM
                        V_NAME_MST ";
        $query .= " WHERE
                        NAMECD1 = '{$namecd}' ";
        $query .= " AND YEAR    = '{$model->year}'";
        $query .= " ORDER BY ";
        $query .= "     ABBV3, ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //災害時帰宅グループ
    public function getGoHome()
    {
        $query  = " SELECT ";
        $query .= "     GO_HOME_GROUP_NO || ':' || GO_HOME_GROUP_NAME AS LABEL, ";
        $query .= "     GO_HOME_GROUP_NO AS VALUE ";
        $query .= " FROM ";
        $query .= "     GO_HOME_GROUP_MST ";

        return $query;
    }

    //責任者
    public function getResponsibility()
    {
        $query .= " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'H109' ";

        return $query;
    }

    //通学経路登録（スクールバス）対象の通学方法取得
    public function getSchoolBus($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     NAMECD1     = 'H100' AND ";
        $query .= "     NAMESPARE1  = '1' ";

        return $query;
    }

    //路線名、駅名を取得
    public function getStationName($station_cd)
    {
        $query  = " SELECT ";
        $query .= "     STATION_NAME, ";
        $query .= "     LINE_NAME ";
        $query .= " FROM ";
        $query .= "     STATION_NETMST ";
        $query .= " WHERE ";
        $query .= "     STATION_CD = '{$station_cd}' ";
        $query .= " ORDER BY ";
        $query .= "     STATION_SORT, ";
        $query .= "     STATION_CD ";

        return $query;
    }

    //エリア名取得
    public function getArea()
    {
        $query  = " SELECT ";
        $query .= "     AREA_NAME AS LABEL, ";
        $query .= "     AREA_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     AREA_MST ";
        $query .= " ORDER BY ";
        $query .= "     INT(AREA_CD) ";

        return $query;
    }

    //とあるエリアの各路線を取得
    public function getRosen($area_cd)
    {
        if ($area_cd == '3') {
            $query  = " SELECT ";
            $query .= "     LINE_NAME  AS LABEL, ";
            $query .= "     LINE_CD AS VALUE ";
            $query .= " FROM ";
            $query .= "     TRAIN_LINE_MST ";
            $query .= " WHERE ";
            $query .= "     AREA_CD = '{$area_cd}' ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '21') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '22') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '23') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '24') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '25') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '26') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '27') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '28') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '29') ";
        } elseif ($area_cd == '5') {
            $query  = " SELECT ";
            $query .= "     LINE_NAME  AS LABEL, ";
            $query .= "     LINE_CD AS VALUE ";
            $query .= " FROM ";
            $query .= "     TRAIN_LINE_MST ";
            $query .= " WHERE ";
            $query .= "     AREA_CD = '{$area_cd}' ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '30') ";
        } elseif ($area_cd == '6') {
            $query  = " SELECT ";
            $query .= "     LINE_NAME  AS LABEL, ";
            $query .= "     LINE_CD AS VALUE ";
            $query .= " FROM ";
            $query .= "     TRAIN_LINE_MST ";
            $query .= " WHERE ";
            $query .= "     AREA_CD = '{$area_cd}' ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '31') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '32') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '33') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '34') ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '35') ";
        } elseif ($area_cd == '9') {
            $query  = " SELECT ";
            $query .= "     LINE_NAME  AS LABEL, ";
            $query .= "     LINE_CD AS VALUE ";
            $query .= " FROM ";
            $query .= "     TRAIN_LINE_MST ";
            $query .= " WHERE ";
            $query .= "     AREA_CD = '{$area_cd}' ";
            $query .= " OR (AREA_CD = '0' AND RR_CD = '36') ";
        } else {
            $query  = " SELECT ";
            $query .= "     LINE_NAME  AS LABEL, ";
            $query .= "     LINE_CD AS VALUE ";
            $query .= " FROM ";
            $query .= "     TRAIN_LINE_MST ";
            $query .= " WHERE ";
            $query .= "     AREA_CD = '{$area_cd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     RR_CD, ";
        $query .= "     LINE_SORT ";

        return $query;
    }

    //とある路線の各駅を取得
    public function getStation($rosen_cd)
    {
        $query  = " SELECT ";
        $query .= "     STATION_NAME AS LABEL, ";
        $query .= "     STATION_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     STATION_NETMST ";
        $query .= " WHERE ";
        $query .= "     LINE_CD = '{$rosen_cd}' ";
        $query .= " ORDER BY ";
        $query .= "     STATION_SORT, ";
        $query .= "     STATION_CD ";

        return $query;
    }

    //通学方法名称取得
    public function getHowToCommutecdName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'H100' AND ";
        $query .= "     NAMECD2 = '1' ";

        return $query;
    }

    //バスコース取得
    public function getBusCourse($bus_course = "")
    {
        $query  = " SELECT ";
        $query .= "     COURSE_CD AS VALUE, ";
        $query .= "     BUS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHILDCARE_BUS_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($bus_course == "") {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query .= " AND COURSE_CD = '".$bus_course."' ";
        }

        return $query;
    }

    //SCHREG_ENVIR_DATにレコードがあるかどうかチェック
    public function insCount($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS INCHECK ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENVIR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //SCHREG_ENVIR_DATを削除
    public function getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_ENVIR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //SCHREG_ENVIR_DATにインサート
    public function getInsertQuery($model, $field)
    {
        $row = knjh010aQuery::insCount($model);

        $db = Query::dbCheckOut();

        $data["SCHREGNO"][TEXT]          = $model->schregno;
        $data["OLD_CRAM"][TEXT]          = $field["OLD_CRAM"];                  //入学以前通っていた塾
        $data["CUR_CRAMCD"][TEXT]        = sprintf("%d", $field["CUR_CRAMCD"]);  //現在通っている塾コンボ
        $data["CUR_CRAM"][TEXT]          = $field["CUR_CRAM"];                  //現在通っている塾名
        $data["STUDYTIME"][TEXT]         = sprintf("%d", $field["STUDYTIME"]);   //学習時間コンボ
        $data["LESSONCD"][TEXT]          = sprintf("%d", $field["LESSONCD"]);    //けいこごとコンボ
        $data["LESSON"][TEXT]            = $field["LESSON"];                    //けいこごと
        $data["PRIZES"][TEXT]            = $field["PRIZES"];                    //賞罰・検定・その他
        $data["BRO_SISCD"][TEXT]         = sprintf("%d", $field["BRO_SISCD"]);   //兄弟姉妹調査コンボ
        $data["RESIDENTCD"][TEXT]        = sprintf("%d", $field["RESIDENTCD"]);  //住居調査コンボ
        $data["SPORTS"][TEXT]            = $field["SPORTS"];                    //スポーツ
        $data["FRIENDSHIP"][TEXT]        = $field["FRIENDSHIP"];                //交友
        $data["PLANUNIV"][TEXT]          = $field["PLANUNIV"];                  //卒業後の志望：進学
        $data["PLANJOB"][TEXT]           = $field["PLANJOB"];                   //卒業後の志望：就職
        $data["ED_ACT"][TEXT]            = $field["ED_ACT"];                    //特別教育活動
        $data["REMARK"][TEXT]            = $field["REMARK"];                    //備考
        $data["REMARK1"][TEXT]           = $field["REMARK1"];                   //住居調査１段目
        $data["REMARK2"][TEXT]           = $field["REMARK2"];                   //住居調査２段目

        $data["GO_HOME_GROUP_NO"][TEXT]  = sprintf("%02d", $field["GO_HOME_GROUP_NO"]); //災害時帰宅グループ番号
        $data["RESPONSIBILITY"][TEXT]    = sprintf("%d", $field["RESPONSIBILITY"]);   //責任者
        $data["COMMUTE_HOURS"][TEXT]     = sprintf("%02d", $field["COMMUTE_HOURS"]);    //通学：所要時間(時)
        $data["COMMUTE_MINUTES"][TEXT]   = sprintf("%02d", $field["COMMUTE_MINUTES"]);  //通学：所要時間(分)
        $data["HOWTOCOMMUTECD"][TEXT]    = $field["HOWTOCOMMUTECD"];   //通学方法コンボ
        $data["UP_DOWN"][TEXT]           = sprintf("%d", $field["UP_DOWN"]);          //上り下り

        for ($i = 1; $i <= 7; $i++) {
            if ($field["FLG_".$i] == "1") {
                $josya = $field["HIDDEN_JOSYA_".$i];
                $rosen = $field["HIDDEN_ROSEN_".$i];
                $gesya = $field["HIDDEN_GESYA_".$i];
                $data["JOSYA_".$i][TEXT]           = $josya;            //通学 乗車駅1
                $data["ROSEN_".$i][TEXT]           = $rosen;            //通学 路線1
                $data["GESYA_".$i][TEXT]           = $gesya;            //通学 下車駅1
            } elseif ($field["FLG_".$i] == "3") {
                $data["JOSYA_".$i][TEXT]           = $field["JOSYA_".$i];           //通学 乗車駅1
                $data["ROSEN_".$i][TEXT]           = $field["HIDDEN_ROSEN_".$i];    //通学 路線1
                $data["GESYA_".$i][TEXT]           = $field["GESYA_".$i];           //通学 下車駅1
            } else {
                $data["JOSYA_".$i][TEXT]           = $field["JOSYA_".$i];  //通学 乗車駅1
                $data["ROSEN_".$i][TEXT]           = $field["ROSEN_".$i];  //通学 路線1
                $data["GESYA_".$i][TEXT]           = $field["GESYA_".$i];  //通学 下車駅1
            }
            $data["FLG_".$i][TEXT]             = $field["FLG_".$i];        //通学手段1
        }

        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][FUNC]           = "sysdate()";

        if ($row["INCHECK"] > 0) {
            $where  = " WHERE SCHREGNO   = '{$model->schregno}'";
            $query = Query::updateSQL($data, "SCHREG_ENVIR_DAT", $where);
        } else {
            $query = Query::insertSQL($data, "SCHREG_ENVIR_DAT");
        }
        $db->query($query);

        Query::dbCheckIn($db);
        return ;
    }

    //SCHREG_ENVIR_DATを更新
    public function getUpdateQuery($model, $field)
    {
        $row = knjh010aQuery::insCount($model);

        $db = Query::dbCheckOut();

        $data = array();
        $data["READING"][TEXT]          = $field["READING"];            //読書傾向
        $data["MERITS"][TEXT]           = $field["MERITS"];             //性質 長所
        $data["DEMERITS"][TEXT]         = $field["DEMERITS"];           //性質 短所
        $data["GOOD_SUBJECT"][TEXT]     = $field["GOOD_SUBJECT"];       //学業 得意科目
        $data["BAD_SUBJECT"][TEXT]      = $field["BAD_SUBJECT"];        //学業 不得意科目
        $data["HOBBY"][TEXT]            = $field["HOBBY"];              //趣味・娯楽
        $data["ATTENTIONMATTERS"][TEXT] = $field["ATTENTIONMATTERS"];   //要注意事項
        $data["DISEASE"][TEXT]          = $field["DISEASE"];            //既往の疾患
        $data["HEALTHCONDITION"][TEXT]  = $field["HEALTHCONDITION"];    //現在の健康状態
        $data["POCKETMONEYCD"][TEXT]    = sprintf("%d", $field["POCKETMONEYCD"]); //こづかい(コンボ)
        $data["POCKETMONEY"][NUMBER]    = $field["POCKETMONEY"];        //こづかい(額)
        if (is_numeric($field["BEDTIME_HOURS"])) {      //睡眠 就寝時
            $data["BEDTIME_HOURS"][TEXT]      = sprintf("%02d", $field["BEDTIME_HOURS"]);
        } else {
            $data["BEDTIME_HOURS"][FUNC]      = "null";
        }
        if (is_numeric($field["BEDTIME_MINUTES"])) {    //睡眠 就寝分
            $data["BEDTIME_MINUTES"][TEXT]    = sprintf("%02d", $field["BEDTIME_MINUTES"]);
        } else {
            $data["BEDTIME_MINUTES"][FUNC]    = "null";
        }
        if (is_numeric($field["RISINGTIME_HOURS"])) {   //睡眠 起床時間
            $data["RISINGTIME_HOURS"][TEXT]   = sprintf("%02d", $field["RISINGTIME_HOURS"]);
        } else {
            $data["RISINGTIME_HOURS"][FUNC]   = "null";
        }
        if (is_numeric($field["RISINGTIME_MINUTES"])) { //睡眠 起床分
            $data["RISINGTIME_MINUTES"][TEXT] = sprintf("%02d", $field["RISINGTIME_MINUTES"]);
        } else {
            $data["RISINGTIME_MINUTES"][FUNC] = "null";
        }
        $data["TVVIEWINGHOURSCD"][TEXT] = sprintf("%d", $field["TVVIEWINGHOURSCD"]); //テレビの視聴時間コンボ
        $data["TVPROGRAM"][TEXT]        = $field["TVPROGRAM"];                      //主に見るテレビ
        $data["PC_HOURS"][TEXT]         = sprintf("%d", $field["PC_HOURS"]);         //パソコンの時間コンボ
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        if ($row["INCHECK"] > 0) {
            $where  = " WHERE SCHREGNO  = '{$model->schregno}' ";
            $db->query(Query::updateSQL($data, "SCHREG_ENVIR_DAT", $where));
        } else {
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $db->query(Query::insertSQL($data, "SCHREG_ENVIR_DAT"));
        }
        Query::dbCheckIn($db);
        return ;
    }
}

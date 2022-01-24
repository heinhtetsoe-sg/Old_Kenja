<?php
/**
 * 定数定義
*/

class knjl363kModel extends Model {
    var $field      = array();
    var $cmd;
    var $ObjYear;
    var $testdiv;
    var $jhflg;
    var $control;   //コントロールマスタ
    var $staffcd;   //職員コード
        
    /**
     * メンバ変数の初期化
     */
    function knjl363kModel()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
        $this->staffcd      = STAFFCD;                  //職員コード
        $this->ObjYear = CTRL_YEAR+1;
    }
    function init()
    {
        $this->cmd          = VARS::request("cmd");
        $this->testdiv      = VARS::post("TESTDIV");
        $this->jhflg        = VARS::post("JHFLG");
    }
    /**
     * 変数をクリアします
     */
    function clean()
    {
    }

    function getDownloadModel()
    {
        $header = array("EXAMNO"            => "受験番号",
                        "NAME"              => "氏名(漢字)",
                        "NAME_KANA"         => "氏名(かな)",
                        "ABBV1"             => "性別",
                        "BIRTHDAY"          => "生年月日",
                        "ADDRESSCD"         => "現住所コード",
                        "ADDR"              => "住所",
                        "TELNO"             => "電話番号",
                        "GNAME"             => "保護者氏名(漢字)",
                        "GKANA"             => "保護者氏名(かな)",
                        "LOCATIONCD"        => "所在地コード(出身学校)",
                        "NATPUBPRIDIV"      => "国公私立区分コード",
                        "FS_CD"             => "出身学校コード",
                        "FINSCHOOL_KANA"    => "出身学校名(よみ)",
                        "FINSCHOOL_NAME"    => "出身学校名",
                        "FINSCHOOL_ADDR1"   => "出身学校住所１",
                        "FINSCHOOL_ADDR2"   => "出身学校住所２",
                        "PRISCHOOL_NAME"    => "塾名",
                        "PS_CD"             => "塾コード",
                        "NAME1"             => "出願区分",
                        "DESIREDIV"         => "出願パターン",
                        "L006ABBV"          => "専/併",
                        "SPECIAL_REASON_DIV" => "特別理由区分",
                        "EXAMCOURSE_NAME"   => "合格コース",
                        "JUDGENM"           => "合否",
                        "SCALASHIPDIV"      => "スカラシップコード",
                        "A_TOTAL"           => "入試得点(500点)",
                        "A_KOKU"            => "国語",
                        "A_SYA"             => "社会",
                        "A_SU"              => "数学",
                        "A_RIKA"            => "理科",
                        "A_ENG"             => "英語",
                        "B_TOTAL"           => "入試得点(550点)",
                        "B_KOKU"            => "国語",
                        "B_SYA"             => "社会",
                        "B_SU"              => "数学",
                        "B_RIKA"            => "理科",
                        "B_ENG"             => "英語",
//NO001 NO002 NO003-->
                        "APPMSJ1"           => "確定コース・専願・判定1",
                        "APPMSJ2"           => "確定コース・専願・判定2",
                        "APPMSJ3"           => "確定コース・専願・判定3",
                        "APPMSJ4"           => "確定コース・専願・判定4",
                        "APPMHJ1"           => "確定コース・併願・判定1",
                        "APPMHJ2"           => "確定コース・併願・判定2",
                        "APPMHJ3"           => "確定コース・併願・判定3",
                        "APPMHJ4"           => "確定コース・併願・判定4",
//NO001 NO002 NO003<--
                        "FS_ITEM1"          => "中学実力得点",
                        "FS_ITEM2"          => "中学実力平均",
                        "FS_ITEM4"          => "中学五ツ木",
                        "FS_ITEM5"          => "中学各種模擬",
//NO001 NO002 NO003-->
                        "FS_COURSE_S_JUD1"  => "中学事前コース・専願・判定1",
                        "FS_COURSE_S_JUD2"  => "中学事前コース・専願・判定2",
                        "FS_COURSE_S_JUD3"  => "中学事前コース・専願・判定3",
                        "FS_COURSE_S_JUD4"  => "中学事前コース・専願・判定4",
                        "FS_COURSE_H_JUD1"  => "中学事前コース・併願・判定1",
                        "FS_COURSE_H_JUD2"  => "中学事前コース・併願・判定2",
                        "FS_COURSE_H_JUD3"  => "中学事前コース・併願・判定3",
                        "FS_COURSE_H_JUD4"  => "中学事前コース・併願・判定4",
//NO001 NO002 NO003<--
                        "PS_ITEM1"          => "塾実力得点",
                        "PS_ITEM2"          => "塾実力平均",
                        "PS_ITEM4"          => "塾五ツ木",
                        "PS_ITEM5"          => "塾各種模擬",
//NO001 NO002 NO003-->
                        "PS_COURSE_S_JUD1"  => "塾事前コース・専願・判定1",
                        "PS_COURSE_S_JUD2"  => "塾事前コース・専願・判定2",
                        "PS_COURSE_S_JUD3"  => "塾事前コース・専願・判定3",
                        "PS_COURSE_S_JUD4"  => "塾事前コース・専願・判定4",
                        "PS_COURSE_H_JUD1"  => "塾事前コース・併願・判定1",
                        "PS_COURSE_H_JUD2"  => "塾事前コース・併願・判定2",
                        "PS_COURSE_H_JUD3"  => "塾事前コース・併願・判定3",
                        "PS_COURSE_H_JUD4"  => "塾事前コース・併願・判定4"
//NO001 NO002<--
                        );

        $contents = implode($header, ",")."\n";
        $data = false;

        $db = Query::dbCheckOut();
        //SQL文発行
        $query = knjl363kQuery::selectCvsQuery($this);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            //データ設定
            $contents .= $row["EXAMNO"].","
                        .$row["NAME"].","
                        .$row["NAME_KANA"].","
                        .$row["ABBV1"].","
                        .$row["BIRTHDAY"].","
                        .$row["ADDRESSCD"].","
                        .$row["ADDR"].","
                        .$row["TELNO"].","
                        .$row["GNAME"].","
                        .$row["GKANA"].","
                        .$row["LOCATIONCD"].","
                        .$row["NATPUBPRIDIV"].","
                        .$row["FS_CD"].","
                        .$row["FINSCHOOL_KANA"].","
                        .$row["FINSCHOOL_NAME"].","
                        .$row["FINSCHOOL_ADDR1"].","
                        .$row["FINSCHOOL_ADDR2"].","
                        .$row["PRISCHOOL_NAME"].","
                        .$row["PS_CD"].","
                        .$row["NAME1"].","
                        .$row["DESIREDIV"].","
                        .$row["L006ABBV"].","
                        .$row["SPECIAL_REASON_DIV"].","
                        .$row["EXAMCOURSE_NAME"].","
                        .$row["JUDGENM"].","
                        .$row["SCALASHIPDIV"].","
                        .$row["A_TOTAL"].","
                        .$row["A_KOKU"].","
                        .$row["A_SYA"].","
                        .$row["A_SU"].","
                        .$row["A_RIKA"].","
                        .$row["A_ENG"].","
                        .$row["B_TOTAL"].","
                        .$row["B_KOKU"].","
                        .$row["B_SYA"].","
                        .$row["B_SU"].","
                        .$row["B_RIKA"].","
                        .$row["B_ENG"].","
//NO002-->
                        .$row["APPMS1"].$row["APPS1"].$row["APPJS1"].","
                        .$row["APPMS2"].$row["APPS2"].$row["APPJS2"].","
                        .$row["APPMS3"].$row["APPS3"].$row["APPJS3"].","
                        .$row["APPMS4"].$row["APPS4"].$row["APPJS4"].","
//NO001-->
                        .$row["APPMH1"].$row["APPH1"].$row["APPJH1"].","
                        .$row["APPMH2"].$row["APPH2"].$row["APPJH2"].","
                        .$row["APPMH3"].$row["APPH3"].$row["APPJH3"].","
                        .$row["APPMH4"].$row["APPH4"].$row["APPJH4"].","
//NO001<--
//NO002<--
/**NO002-->
                        .$row["APPMS1"].$row["APPJS1"].","
                        .$row["APPMS2"].$row["APPJS2"].","
                        .$row["APPMS3"].$row["APPJS3"].","
                        .$row["APPMS4"].$row["APPJS4"].","
//NO001-->
                        .$row["APPMH1"].$row["APPJH1"].","
                        .$row["APPMH2"].$row["APPJH2"].","
                        .$row["APPMH3"].$row["APPJH3"].","
                        .$row["APPMH4"].$row["APPJH4"].","
//NO001<--
<--NO002***/
                        .$row["FS_ITEM1"].","
                        .$row["FS_ITEM2"].","
                        .$row["FS_ITEM4"].","
                        .$row["FS_ITEM5"].",";  //NO001
//NO002-->
            $contents .= $row["FS_JUDG1"].","
                        .$row["FS_JUDG2"].","
                        .$row["FS_JUDG3"].","
                        .$row["FS_JUDG4"].",";
            $contents .= $row["FH_JUDG1"].","
                        .$row["FH_JUDG2"].","
                        .$row["FH_JUDG3"].","
                        .$row["FH_JUDG4"].",";
//NO002<--
/**NO002-->
//NO001-->
            if ($row["FS_SHDIV1"] = "S"){
                $contents .= $row["FS_COURSEMARK1"].$row["FS_JUDG1"].",";
            }else {
                $contents .= ",";
            }
            if ($row["FS_SHDIV2"] = "S"){
                $contents .= $row["FS_COURSEMARK2"].$row["FS_JUDG2"].",";
            }else {
                $contents .= ",";
            }
            if ($row["FS_SHDIV3"] = "S"){
                $contents .= $row["FS_COURSEMARK3"].$row["FS_JUDG3"].",";
            }else {
                $contents .= ",";
            }
            if ($row["FS_SHDIV4"] = "S"){
                $contents .= $row["FS_COURSEMARK4"].$row["FS_JUDG4"].",";
            }else {
                $contents .= ",";
            }

            if ($row["FS_SHDIV1"] = "H"){
                $contents .= $row["FS_COURSEMARK1"].$row["FS_JUDG1"].",";
            }else {
                $contents .= ",";
            }
            if ($row["FS_SHDIV2"] = "H"){
                $contents .= $row["FS_COURSEMARK2"].$row["FS_JUDG2"].",";
            }else {
                $contents .= ",";
            }
            if ($row["FS_SHDIV3"] = "H"){
                $contents .= $row["FS_COURSEMARK3"].$row["FS_JUDG3"].",";
            }else {
                $contents .= ",";
            }
            if ($row["FS_SHDIV4"] = "H"){
                $contents .= $row["FS_COURSEMARK4"].$row["FS_JUDG4"].",";
            }else {
                $contents .= ",";
            }
//NO001<--
<--NO002***/
            $contents .= $row["PS_ITEM1"].","
                        .$row["PS_ITEM2"].","
                        .$row["PS_ITEM4"].","
                        .$row["PS_ITEM5"].",";  //NO001
//NO002-->
            $contents .= $row["PS_JUDG1"].","
                        .$row["PS_JUDG2"].","
                        .$row["PS_JUDG3"].","
                        .$row["PS_JUDG4"].",";
            $contents .= $row["PH_JUDG1"].","
                        .$row["PH_JUDG2"].","
                        .$row["PH_JUDG3"].","
                        .$row["PH_JUDG4"].",";
//NO002<--
/**NO002-->
//NO001-->
            if ($row["FS_SHDIV1"] = "S"){
                $contents .= $row["FS_COURSEMARK1"].$row["FS_JUDG1"].",";
            }else {
                $contents .= ",";
            }
            if ($row["FS_SHDIV2"] = "S"){
                $contents .= $row["FS_COURSEMARK2"].$row["FS_JUDG2"].",";
            }else {
                $contents .= ",";
            }
            if ($row["FS_SHDIV3"] = "S"){
                $contents .= $row["FS_COURSEMARK3"].$row["FS_JUDG3"].",";
            }else {
                $contents .= ",";
            }
            if ($row["FS_SHDIV4"] = "S"){
                $contents .= $row["FS_COURSEMARK4"].$row["FS_JUDG4"].",";
            }else {
                $contents .= ",";
            }

            if ($row["FS_SHDIV1"] = "H"){
                $contents .= $row["FS_COURSEMARK1"].$row["FS_JUDG1"].",";
            }else {
                $contents .= ",";
            }
            if ($row["FS_SHDIV2"] = "H"){
                $contents .= $row["FS_COURSEMARK2"].$row["FS_JUDG2"].",";
            }else {
                $contents .= ",";
            }
            if ($row["FS_SHDIV3"] = "H"){
                $contents .= $row["FS_COURSEMARK3"].$row["FS_JUDG3"].",";
            }else {
                $contents .= ",";
            }
            if ($row["FS_SHDIV4"] = "H"){
                $contents .= $row["FS_COURSEMARK4"].$row["FS_JUDG4"].",";
            }else {
                $contents .= ",";
            }
//NO001<--
<--NO002***/
            $contents .= "\n";
            $data = true;
        }
        $result->free();

        Query::dbCheckIn($db);

        if ($this->jhflg == 1){
            $jhname = "中学校";
        }else {
            $jhname = "高等学校";
        }

        if($data){
            $filename = $this->ObjYear."年度".$jhname."入学受験者データ.csv";
            //ファイルダウンロード
            common::downloadContents($contents, $filename);

            return true;
        }else{
            $this->setWarning("MSG303");    //データは存在していません。
            return false;
        }
    }

}
?>

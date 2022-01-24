<?php

require_once('for_php7.php');

class knjz290Query extends Query {

    //職員年度データより、指定年度に登録されているデータを取得
    function selectQuery($model)
    {
        $query =  "select D.STAFFCD as STAFFCD, M.LNAME_SHOW || M.FNAME_SHOW as STAFFNAME,";
		$query .= "D.DUTYSHARECD as DUTYSHARECD,D.JOBNAMECD as JOBNAMECD,D.STAFFSEC_CD as STAFFSEC_CD,";
		$query .= "D.CHARGECLASSCD as CHARGECLASSCD,D.REQUIRECLUBCD as REQUIRECLUBCD,D.EXTRACLUBCD as EXTRACLUBCD ";
		$query .= "FROM staffyear_dat D, staff_mst M ";
        $query .= "where D.STAFFCD = M.STAFFCD ";
        $query .= "and D.STAFFYEAR = '".$model->year."'" ;
        $query .= "order by D.STAFFCD";
        return $query;
    }
    
    //所属年度一覧取得
    function selectYearQuery()
    {
        return "SELECT DISTINCT YEAR FROM staff_ydat ORDER BY YEAR DESC";
    }
    
	//所属一覧取得（マスタにあって年度データにないデータ）
    function selectstaffQuery($staffcd_array,$model)
    {
        $staffcd = implode($staffcd_array,"','");
		$query = "SELECT STAFFCD, LNAME_SHOW || FNAME_SHOW AS STAFFMASTERNAME FROM staff_mst ";
        $query .= "WHERE STAFFCD NOT IN ('".$staffcd."') ORDER BY STAFFCD ";
		return $query;
    }

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////ADDUPDATEDELETE/////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function &getAddUpdDelQuery(&$model)
	{
		//所属コード配列に格納
//		$array = explode (",", $model->selectdata);
		$aacd = explode (",", $model->addcd);
		$aajob = explode (",", $model->addjob);
		$aasec = explode (",", $model->addsec);
		$aasha = explode (",", $model->addsha);
		$aacla = explode (",", $model->addcla);
		$aahcl = explode (",", $model->addhcl);
		$aascl = explode (",", $model->addscl);
		$aucd = explode (",", $model->updcd);
		$aujob = explode (",", $model->updjob);
		$ausec = explode (",", $model->updsec);
		$ausha = explode (",", $model->updsha);
		$aucla = explode (",", $model->updcla);
		$auhcl = explode (",", $model->updhcl);
		$auscl = explode (",", $model->updscl);
		$adcd = explode (",", $model->delcd);
		$adjob = explode (",", $model->deljob);
		$adsec = explode (",", $model->delsec);
		$adsha = explode (",", $model->delsha);
		$adcla = explode (",", $model->delcla);
		$adhcl = explode (",", $model->delhcl);
		$adscl = explode (",", $model->delscl);

		$updated = date("Y-m-d H:i:s", time()); //更新日付

		//追加
		$db = Query::dbCheckOut();
		$db->autoCommit(false);
//		$query = "DELETE from jobyear_dat where JOBYEAR = '" .$model->year ."'";
//		$db->query($query);

		if ($aacd[0] != "")
		{
			$query = "INSERT INTO staffyear_dat(STAFFYEAR,STAFFCD,DUTYSHARECD,JOBNAMECD,STAFFSEC_CD,REQUIRECLUBCD,";
			$query.= "EXTRACLUBCD,CHARGECLASSCD,UPDATED) VALUES";
			for ($i = 0; $i < get_count($aacd); $i++)
			{
				$sp = ",";
				if ($i == get_count($aacd)-1)
				{
					$sp = "";
				}
				$query.= "('" .$model->year ."'";                               //年度
				$query.= ",'" .$aacd[$i] ."'";                                 //職員コード
				$query.= ",'" .$aasha[$i] ."'";                                 //校務分掌部コード
				$query.= ",'" .$aajob[$i] ."'";                                 //職名コード
				$query.= ",'" .$aasec[$i] ."'";                                 //職員所属所属コード
				$query.= ",'" .$aahcl[$i] ."'";                                 //必修クラブコード
				$query.= ",'" .$aascl[$i] ."'";                                 //課外クラブコード
				$query.= ",'" .$aacla[$i] ."'";                                 //授業受持区分
				$query.= ",'" .$updated ."')" .$sp;                             //更新日付   
			}
			$db->query($query);
			$db->commit(); // トランザクションをコミットする。
			Query::dbCheckIn($db);
		}

		//更新
		if ($aucd[0] != "")
		{
			for ($i = 0; $i < get_count($aucd); $i++)
			{
				$db = Query::dbCheckOut();
				$db->autoCommit(false);
				$query = "UPDATE staffyear_dat SET ";
				$query.= "DUTYSHARECD='" .$ausha[$i] ."'";                                 //校務分掌部コード
				$query.= ",JOBNAMECD='" .$aujob[$i] ."'";                                 //職名コード
				$query.= ",STAFFSEC_CD='" .$ausec[$i] ."'";                                 //職員所属所属コード
				$query.= ",REQUIRECLUBCD='" .$auhcl[$i] ."'";                               //必修クラブコード
				$query.= ",EXTRACLUBCD='" .$auscl[$i] ."'";                                 //課外クラブコード
				$query.= ",CHARGECLASSCD='" .$aucla[$i] ."'";                                 //授業受持区分
				$query.= ",UPDATED='" .$updated ."' ";                             //更新日付   
				$query.= "WHERE STAFFYEAR='" .$model->year ."' ";						//職員年度
				$query.= "AND STAFFCD='" .$aucd[$i] ."'";							//職員コード
				$db->query($query);
				$db->commit(); // トランザクションをコミットする。
				Query::dbCheckIn($db);
			}
		}

		//削除
		if ($adcd[0] != "")
		{
			for ($i = 0; $i < get_count($adcd); $i++)
			{
				$db = Query::dbCheckOut();
				$db->autoCommit(false);
				$query = "DELETE FROM staffyear_dat ";
				$query.= "WHERE STAFFYEAR='" .$model->year ."' ";						//職員年度
				$query.= "AND STAFFCD='" .$adcd[$i] ."'";								//職員コード
				$db->query($query);
				$db->commit(); // トランザクションをコミットする。
				Query::dbCheckIn($db);
			}
		}

///		$model->year = "";
		return;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	function getCopyQuery(&$model)
	{
		$copy1 = explode (",", $model->copy1);			//職員コード
		$copy2 = explode (",", $model->copy2);			//職名コード
		$copy3 = explode (",", $model->copy3);			//所属コード
		$copy4 = explode (",", $model->copy4);			//受持授業
		$updated = date("Y-m-d H:i:s", time()); //更新日付

		//前年度のデータと同じ内容を追加
		$db = Query::dbCheckOut();
		$db->autoCommit(false);

		$model->year = $model->year + 1;		//次年度にコピー
		$query = "INSERT INTO staffyear_dat(STAFFYEAR,STAFFCD,JOBNAMECD,STAFFSEC_CD,CHARGECLASSCD,UPDATED) VALUES";
		for ($i = 0; $i < get_count($copy1); $i++)
		{
			$sp = ",";
			if ($i == get_count($copy1)-1)
			{
				$sp = "";
			}
			$query.= "('" .$model->year ."'";									//年度
			$query.= ",'" .$copy1[$i] ."'";									//職員コード
			$query.= ",'" .$copy2[$i] ."'"; 								//職名コード
			$query.= ",'" .$copy3[$i] ."'"; 								//職員所属所属コード
			$query.= ",'" .$copy4[$i] ."'"; 								//授業受持区分
			$query.= ",'" .$updated ."')" .$sp; 							//更新日付	 
		}
		$db->query($query);
		$db->commit(); // トランザクションをコミットする。
		Query::dbCheckIn($db);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	//職名リスト作成
	function getVJobMst_data($wrkyear)
	{
//		$query =  "SELECT * FROM v_job_mst";
		$query =  "SELECT M.JOBCD,M.JOBNAME ";
		$query .= "FROM JOB_MST M INNER JOIN JOBYEAR_DAT D ON M.JOBCD = D.JOBCD ";
		$query .= "WHERE D.JOBYEAR ='".$wrkyear."'";
		$query .= " ORDER BY M.JOBCD";
		return $query;
	}

	//所属リスト作成
	function getVSectionMst_data($wrkyear)
	{
//		$query =  "SELECT * FROM v_section_mst";
		$query =  "SELECT M.SECTIONCD,M.SECTIONNAME ";
		$query .= "FROM SECTION_MST M INNER JOIN SECTIONYEAR_DAT D ON M.SECTIONCD = D.SECTIONCD ";
		$query .= "WHERE D.SECTIONYEAR ='".$wrkyear."'";
		$query .= " ORDER BY M.SECTIONCD";
		return $query;
	}

	//校務分掌部リスト作成
	function getVDutyshareMst_data($wrkyear)
	{
//		$query =  "SELECT * FROM v_dutyshare_mst";
		$query =  "SELECT M.DUTYSHARECD,M.SHARENAME ";
		$query .= "FROM DUTYSHARE_MST M INNER JOIN DUTYSHAREYEAR_DAT D ON M.DUTYSHARECD = D.DUTYSHARECD ";
		$query .= "WHERE D.DUTYSHAREYEAR ='".$wrkyear."'";
		$query .= " ORDER BY M.DUTYSHARECD";
		return $query;
	}

	//必修・課外クラブコードリスト作成
	function getVClubMst_data($wrkyear)
	{
//		$query =  "SELECT * FROM v_club_mst";
		$query =  "SELECT M.CLUBCD,M.CLUBNAME ";
		$query .= "FROM CLUB_MST M INNER JOIN CLUB_YEAR_DAT D ON M.CLUBCD = D.CLUBCD ";
		$query .= "WHERE D.CLUBYEAR ='".$wrkyear."'";
		$query .= " ORDER BY M.CLUBCD";
		return $query;
	}

	//次年度の職名年度データが作成されているかどうかチェック
	function getNextJob($newy)
	{
		$db = Query::dbCheckOut();

		$query = "SELECT * FROM JOBYEAR_DAT ".
				 " WHERE JOBYEAR = '" .$newy ."'";

		$row = $db->getRow($query, DB_FETCHMODE_ASSOC);
		Query::dbCheckIn($db);
		return $row;
	}

	//次年度の職員所属年度データが作成されているかどうかチェック
	function getNextSection($newy)
	{
		$db = Query::dbCheckOut();

		$query = "SELECT * FROM SECTIONYEAR_DAT ".
				 " WHERE SECTIONYEAR = '" .$newy ."'";

		$row = $db->getRow($query, DB_FETCHMODE_ASSOC);
		Query::dbCheckIn($db);
		return $row;
	}


}
?>

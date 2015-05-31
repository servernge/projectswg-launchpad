/*
 * 
 * This file is part of ProjectSWG Launchpad.
 *
 * ProjectSWG Launchpad is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * ProjectSWG Launchpad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with ProjectSWG Launchpad.  If not, see <http://www.gnu.org/licenses/>.      
 *
 */

package com.projectswg.launchpad.model;

import javafx.scene.control.Button;

import com.projectswg.launchpad.controller.GameController;
import com.projectswg.launchpad.service.GameService;


public class SWG
{
	public static final String[] FILES = {

		"SwgClientSetup_r.exe",
		"BugTool.exe",

		"bottom.tre",
		"data_animation_00.tre",
		"data_music_00.tre",
		"data_other_00.tre",
		"data_sample_00.tre",
		"data_sample_01.tre",
		"data_sample_02.tre",
		"data_sample_03.tre",
		"data_sample_04.tre",
		"data_skeletal_mesh_00.tre",
		"data_skeletal_mesh_01.tre",
		"data_static_mesh_00.tre",
		"data_static_mesh_01.tre",
		"data_texture_00.tre",
		"data_texture_01.tre",
		"data_texture_02.tre",
		"data_texture_03.tre",
		"data_texture_04.tre",
		"data_texture_05.tre",
		"data_texture_06.tre",
		"data_texture_07.tre",
		"default_patch.tre",

		"dbghelp.dll",
		"dpvs.dll",
		"Mss32.dll",
		"qt-mt305.dll"
	};
	
	private GameService gameService;
	private GameController gameController;
	private Button gameButton;
	
	public SWG(GameService gameService)
	{
		this.gameService = gameService;
	}
	
	public void setGameButton(Button gameButton)
	{
		this.gameButton = gameButton;
	}
	
	public Button getGameButton()
	{
		return gameButton;
	}
	
	public void setGameController(GameController gameController)
	{
		this.gameController = gameController;
		gameController.setGameService(gameService);
	}
	
	public GameController getGameController()
	{
		return gameController;
	}
	
	public GameService getGameService()
	{
		return gameService;
	}
}
